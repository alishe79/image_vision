import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'image_vision_platform_interface.dart';

/// An implementation of [ImageVisionPlatform] that uses method channels.
class MethodChannelImageVision extends ImageVisionPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('image_vision');

  @override
  Future<bool> initial() async {
    final init = await methodChannel.invokeMethod<bool>('initial');
    return init ?? false;
  }

  @override
  Future<String> getTagsOfImage(Uint8List uint8list, double confidence) async {
    final tags = await methodChannel.invokeMethod<String>('runOnBytesList', {
      "byteImage": uint8list,
      "confidence": confidence
    });
    return tags ?? "[]";
  }
  @override
  Future<String> detectFacesOnImage(Uint8List uint8list) async {
    final faces = await methodChannel.invokeMethod<String>('detect_faces', {
      "byteImage": uint8list
    });
    return faces ?? "[]";
  }

  @override
  Future<dynamic> recognizeFace(Uint8List uint8list) async {
    final response = await methodChannel.invokeMethod<dynamic>('recognize_face', {
      "byteImage": uint8list
    });
    return response ?? "error";
  }

  @override
  Future<String> registerFace(String name, Uint8List uint8list) async {
    final response = await methodChannel.invokeMethod<dynamic>('register_face', {
      "name": name,
      "byteImage": uint8list
    });
    return response ?? "error";
  }
}
