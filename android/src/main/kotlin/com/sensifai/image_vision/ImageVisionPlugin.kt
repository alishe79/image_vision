package com.sensifai.image_vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.sensifai.image_vision.env.Logger
import com.sensifai.image_vision.ml.Model
import com.sensifai.image_vision.tflite.SimilarityClassifier
import com.sensifai.image_vision.tflite.TFLiteObjectDetectionAPIModel

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException

/** ImageVisionPlugin */
class ImageVisionPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context : Context

  private val LOGGER: Logger = Logger()
  private val TF_OD_API_INPUT_SIZE = 112
  private val TF_OD_API_IS_QUANTIZED = false
  private val TF_OD_API_MODEL_FILE = "mobile_face_net.tflite"


  private val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"

  private val MODE: DetectorMode = DetectorMode.TF_OD_API
  private var detector: SimilarityClassifier? = null

  private enum class DetectorMode {
    TF_OD_API
  }

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "image_vision")
    channel.setMethodCallHandler(this)
    try {
      detector = TFLiteObjectDetectionAPIModel.create(
        context.assets,
        TF_OD_API_MODEL_FILE,
        TF_OD_API_LABELS_FILE,
        TF_OD_API_INPUT_SIZE,
        TF_OD_API_IS_QUANTIZED
      )
      //cropSize = TF_OD_API_INPUT_SIZE;
    } catch (e: IOException) {
      e.printStackTrace()
      LOGGER.e(
        e,
        "Exception initializing classifier!"
      )
      val toast = Toast.makeText(
        context, "Classifier could not be initialized", Toast.LENGTH_SHORT
      )
      toast.show()
    }
  }

  companion object {
    var model: Model? = null
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method){
      "initial" -> {
        try {
          model = Model.newInstance(context)
          result.success(true)
        } catch (e: Exception) {
          Log.e("Image Vision", e.message.toString())
          result.success(false)
        }
      }
      "runOnBytesList" -> {
        val byteArrayImage: ByteArray = call.argument<ByteArray>("byteImage") ?: return
        val confidence: Double = call.argument<Double>("confidence") ?: return
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(512, 512, 3), DataType.UINT8)
        val bitmap = BitmapFactory.decodeByteArray(byteArrayImage, 0, byteArrayImage.size)
        val image = TensorImage.fromBitmap(Bitmap.createScaledBitmap(bitmap, 512, 512, true))
        inputFeature0.loadBuffer(image.buffer)

        if (model == null) {
          result.error(
            "-7",
            "Model not initialed",
            "before run model on image run await _vision.initial(); if response true can you work with this plugin perfectly"
          )
          return
        }
        val t = Thread {
          Runnable {
            Handler(Looper.getMainLooper())
              .postDelayed({
                val outputs = model?.process(inputFeature0)
                if (outputs != null) {
                  processOutput(outputs, confidence, result)
                } else {
                  result.success("[]")
                }

              }, 1000)
          }.run()
        }
        processOutput(t)


      }
      "close" -> {
        try {
          model?.close()
          model = null
          result.success(true)
        } catch (e: Exception) {
          result.success(false)
        }
      }
      "detect_faces" -> {
        val byteArrayImage: ByteArray = call.argument<ByteArray>("byteImage") ?: return
        val options = FaceDetectorOptions.Builder()
          .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
          .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
          .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
          .build()

        val detector: FaceDetector = FaceDetection.getClient(options)
        val bitmap = BitmapFactory.decodeByteArray(byteArrayImage, 0, byteArrayImage.size)
        val image = InputImage.fromBitmap(bitmap, 0)
        detector
          .process(image)
          .addOnSuccessListener(OnSuccessListener<List<Face>> { faces ->
            if (faces.isEmpty()) {
              result.success("[]")
              return@OnSuccessListener
            }
            var json: String = "[\n"
            for (i in faces.indices) {
              val face = faces[i]
              json += "{\n"
              json += "    \"top\": ${face.boundingBox.top},\n"
              json += "    \"left\": ${face.boundingBox.left},\n"
              json += "    \"right\": ${face.boundingBox.right},\n"
              json += "    \"bottom\": ${face.boundingBox.bottom},\n"
              json += "    \"width\": ${face.boundingBox.width()},\n"
              json += "    \"height\": ${face.boundingBox.height()}\n"
              json += "}"
              if (i != (faces.size - 1)) {
                json += ","
              }
              json += "\n"
            }
            json += "]"

            result.success(json)

          }
        )
      }
      "recognize_face" -> {
        val faceBA: ByteArray = call.argument<ByteArray>("byteImage") ?: return
        val faceBmp = BitmapFactory.decodeByteArray(faceBA, 0, faceBA.size)
        val resizedFace = Bitmap.createScaledBitmap(faceBmp, 112, 112, true);
        if (detector != null) {
          val resultsAux: List<SimilarityClassifier.Recognition> = detector!!.recognizeImage(resizedFace, false)
          if (resultsAux.isEmpty()){
            val map: HashMap<String, Any> = HashMap()
            map["title"] = "face_not_found"
            map["confidence"] = 0.0
            result.success(map)
            return
          }
          val res = resultsAux[0]
          val title = res.title
          val conf = res.distance ;
          if (title == "?"){
            val map: HashMap<String, Any> = HashMap()
            map["title"] = "face_not_found"
            map["confidence"] = conf
            map["confidenceString"] = conf
            result.success(map)
            return
          }

          if (conf < 1.0f){
            if (res.id.equals("0")) {
              val map: HashMap<String, Any> = HashMap()
              map["title"] = title.toString()
              map["confidence"] = conf
              map["confidenceString"] = conf
              result.success(map)
            } else {
              val map: HashMap<String, Any> = HashMap()
              map["title"] = "face_not_found"
              map["confidence"] = conf
              map["confidenceString"] = conf
              result.success(map)
            }

          } else {
            val map: HashMap<String, Any> = HashMap()
            map["title"] = "face_not_found"
            map["confidence"] = conf
            map["confidenceString"] = conf
            result.success(map)
          }

        } else {
          val map: HashMap<String, Any> = HashMap()
          map["title"] = "face_not_found"
          map["confidence"] = 0.0
          map["confidenceString"] = "0.0"
          result.success(map)
        }
      }
      "register_face" -> {
        if (detector != null){
          val name: String = call.argument<String>("name") ?: return
          val faceBA: ByteArray = call.argument<ByteArray>("byteImage") ?: return
          val faceBmp = BitmapFactory.decodeByteArray(faceBA, 0, faceBA.size)
          val resizedFace = Bitmap.createScaledBitmap(faceBmp, 112, 112, true)
          val resultsAux: List<SimilarityClassifier.Recognition> = detector!!.recognizeImage(resizedFace, true)
          if (resultsAux.isEmpty()){
            result.success("face_not_found")
            return
          }
          val rec = resultsAux[0]
          detector!!.register(name, rec)
          result.success("registered")
        }
      }
      else -> {
        result.error(
          "MissingPluginException",
          "Unhandled Exception",
          "No implementation found for method"
        )
      }
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun processOutput(thread: Thread) {
    thread.start()
  }

  private fun processOutput(outputs: Model.Outputs, confidence: Double, result: Result) {
    val outputFeature0 = outputs.outputFeature0AsTensorBuffer
    val responses = outputFeature0.floatArray
    val x = ArrayList<Confidence>()
    val labels = Labels(context)
    for (i in responses.indices) {
      if (responses[i] > confidence.toFloat()) {
        val label = labels.getLabelByIndex(i)
        val description = labels.getDescriptionByLabel(label)
        x.add(Confidence(i, responses[i], label, description))
      }
    }

    x.sortWith { o1, o2 ->
      o2.rate.compareTo(o1.rate)
    }

    var response = "[\n"
    for (i in 0 until x.size) {
      response += "   {\n"
      response += "       \"index\": " + x[i].index + ",\n"
      response += "       \"confidence\": " + x[i].rate.toDouble() + ",\n"
      response += "       \"label\": \"" + x[i].label + "\",\n"
      response += "       \"description\": \"" + x[i].description + "\"\n"
      response += "   }"
      response += if ((x.size - 1) != i) {
        ",\n"
      } else {
        "\n"
      }
    }

    response += "]"

    if (x.isNotEmpty()) {
      result.success(response)
    } else {
      result.success("[]")
    }
  }
}
