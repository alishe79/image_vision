
import 'dart:typed_data';

import 'image_vision_platform_interface.dart';

class ImageVision {


  static Future<bool> initial() {
    return ImageVisionPlatform.instance.initial();
  }

  static Future<String> getTagsOfImage(Uint8List image, double confidence) {
    return ImageVisionPlatform.instance.getTagsOfImage(image, confidence);
  }

  static Future<String> detectFacesFromImage(Uint8List image) {
    return ImageVisionPlatform.instance.detectFacesOnImage(image);
  }

  static Future<dynamic> recognizeFace(Uint8List image) {
    return ImageVisionPlatform.instance.recognizeFace(image);
  }

  static Future<String> registerFace(String name, Uint8List image) {
    return ImageVisionPlatform.instance.registerFace(name, image);
  }

}
