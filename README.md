

# Image Vision



![Pub Version](https://img.shields.io/pub/v/vision)  ![LICENSE](https://img.shields.io/badge/license-MIT%20License-green)



### Offline image tagging & Face recognition


- +5000 Labels

- Offline

- Pre-train AI Model

- Support PNG, JPEG, JPG and GIF

- Powered by [TFLite](https://www.tensorflow.org/lite "TFLite") & [Google ML Kit](https://developers.google.com/ml-kit/vision/face-detection "Google ML Kit")



#### Why was this plugin written?

****The strength of this plugin is tagging photos on the device without communicating with anything outside the user's device. Well, the conclusion is that the most important issue for all of us is privacy. Initially, this plugin started with this vision, but it focuses on three main themes. Speed, accuracy and privacy security****



#### What you need ?

****In this example you need add [Imager picker](https://pub.dev/packages/image_picker) plugin****





#### How to use Vision?

****Simply****



```dart

// image_picker plugin
import "dart:developer" as dev;
final ImagePicker picker = ImagePicker();

// Initial vision
var initialed = false ;
Future<bool> init() async {
  initialed = await ImageVision.initial();
}
// Tag image function
Future<List<Map<String, dynamic>>> getLabels(File file) async {  
  var bytes = await file.readAsBytes();  
  String jsonLabels = await ImageVision.getTagsOfImage(Uint8List.fromList(bytes.toList()), 0.3);  
  var labels = List<Map<String, dynamic>>.from(json.decode(jsonLabels));  
  if (kDebugMode){  
    dev.log(labels.toString());  
  }  
  return labels ;  
}  
  
// Detect faces position on image
Future<List<Map<String, dynamic>>> getFaces(File file) async {  
  var bytes = await file.readAsBytes();  
  String jsonLabels = await ImageVision.detectFacesFromImage(Uint8List.fromList(bytes.toList()));  
  var faces = List<Map<String, dynamic>>.from(json.decode(jsonLabels));  
  if (kDebugMode){  
    dev.log(faces.toString());  
  }  
  return faces ;  
}  
  
// crop & recogniton face image
Future<dynamic> recognizeFace(Map<String, dynamic> inputFace, File image) async {  
  var face = img.decodeImage(await image.readAsBytes());  
  if (face != null){  
    face = img.copyCrop(  
        face,  
        int.parse(inputFace["left"].toString()),  
        int.parse(inputFace["top"].toString()),  
        int.parse(inputFace["width"].toString()),  
        int.parse(inputFace["height"].toString()),  
    );  
    final png = img.encodePng(face);  
    var rec = await ImageVision.recognizeFace(Uint8List.fromList(png));  
    var split = rec["confidence"].toString().split(".");  
    var number = split[0];  
    if (int.parse(number) < 1){  
      rec["title"] = "face_not_found";  
      rec["confidence"] = "0.0";  
      return rec;  
    } else {  
      return rec ;  
    }  
  }  
  
  return {};  
}  

// Register new face in Mobile Face net.
Future<String> register(String name, Map<String, dynamic> inputFace, File image) async {  
  
  var face = img.decodeImage(await image.readAsBytes());  
  if (face != null){  
    face = img.copyCrop(  
      face,  
      int.parse(inputFace["left"].toString()),  
      int.parse(inputFace["top"].toString()),  
      int.parse(inputFace["width"].toString()),  
      int.parse(inputFace["height"].toString()),  
    );  
    final png = img.encodePng(face);  
    var rec = await ImageVision.registerFace(name, Uint8List.fromList(png));  
    dev.log(rec.toString());  
    return rec ;  
  }  
  return "error";  
}

```

***Example response of  getTagsOfImage() Method ...***

```json
[
	{
		"index":567,
	    "confidence":0.7956880331039429,
	    "label":"/m/01gq53",
	    "description":"Performance"
    
    },
	{
	    "index":2629,
	    "confidence":0.7695709466934204,
	    "label":"/m/04_5hy",
	    "description":"Stage"
    },
	{
       "index":2990,
       "confidence":0.32111555337905884,
       "label":"/m/0557q",
       "description":"Musical theatre"
    }
]
```

***Example response of  detectFacesFromImage() Method ...***

```json
[
	{
	    "top": 799,
	    "left": 686,
	    "right": 848,
	    "bottom": 961,
	    "width": 162,
	    "height": 162
	},
	{
	    "top": 801,
	    "left": 189,
	    "right": 416,
	    "bottom": 1024,
	    "width": 227,
	    "height": 223
	},
	{
	    "top": 840,
	    "left": 408,
	    "right": 536,
	    "bottom": 968,
	    "width": 128,
	    "height": 128
	}
]
```


### Enjoy :)



#### Created with ❤️🍰☕ at [Sensifai](https://sensifai.com "Sensifai")
#### [Smart gallery](https://smartgallery.sensifai.com "Smart gallery") uses [Image Vision](https://pub.dev/packages/image_vision "Image Vision")