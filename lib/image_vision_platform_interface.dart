import 'dart:typed_data';

import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'image_vision_method_channel.dart';

abstract class ImageVisionPlatform extends PlatformInterface {
  /// Constructs a ImageVisionPlatform.
  ImageVisionPlatform() : super(token: _token);

  static final Object _token = Object();

  static ImageVisionPlatform _instance = MethodChannelImageVision();

  /// The default instance of [ImageVisionPlatform] to use.
  ///
  /// Defaults to [MethodChannelImageVision].
  static ImageVisionPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ImageVisionPlatform] when
  /// they register themselves.
  static set instance(ImageVisionPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool> initial() {
    throw UnimplementedError('initial() has not been implemented.');
  }

  Future<String> getTagsOfImage(Uint8List uint8list, double confidence) {
    throw UnimplementedError('getTagsOfImage(Uint8List uint8list, double confidence) has not been implemented.');
  }

  Future<String> detectFacesOnImage(Uint8List uint8list) {
    throw UnimplementedError('detectFacesOnImage(Uint8List uint8list) has not been implemented.');
  }

  Future<dynamic> recognizeFace(Uint8List uint8list) {
    throw UnimplementedError('detectFacesOnImage(Uint8List uint8list) has not been implemented.');
  }

  Future<String> registerFace(String name, Uint8List uint8list) {
    throw UnimplementedError('registerFace(String name, Uint8List uint8list) has not been implemented.');
  }
}
