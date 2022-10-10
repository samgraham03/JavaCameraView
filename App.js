import React, { useState, useEffect, useRef } from 'react';
import { StyleSheet, TouchableOpacity } from 'react-native';
import { OpenCVCameraView } from './src/native/OpenCVCameraView';
import OpenCVCameraModule from './src/native/OpenCVCameraModule';

const App = () => {
  const BASE64_PREFIX = 'data:image/jpeg;base64,';
  const FILE_PREFIX = 'file://'

  const cameraRef = useRef(null);
  const [renderCamera, setRenderCamera] = useState(false);
  const [renderComponents, setRenderComponents] = useState(false);
  const [capturing, setCapturing] = useState(false);
  const [enableProcessing, setEnableProcessing] = useState(false);
  const [cameraID, setCameraID] = useState(-1);

  const [saveAsBase64, setSaveAsBase64] = useState(false);
  const [image, setImage] = useState('');

  useEffect(() => {
    console.log(cameraID);
  }, [cameraID]);

  useEffect(() => { // TODO: replace with useFocusEffect (React Navigation library)
    if (!capturing) {
      setRenderCamera(true);
    }
  });

  // Called when camera renders to restore native properties / sync with react native
  useEffect(() => {
    if (cameraRef.current !== null) {
      // OpenCVCameraModule.getNativeValue(res => setReactNativeValue(res)); // EXAMPLE
      setRenderComponents(renderCamera);
    }
  }, [renderCamera]);

  const takePic = async (firstCall = true) => {
    setCapturing(true);
    OpenCVCameraModule.takePicture(firstCall, (captured, imagePath) => {
      if (captured === false) {
        takePic(captured);
        console.log('Capture not ready');
      } else {
        console.log(`Image address: ${imagePath}`);
        if (saveAsBase64) {
          setImage(BASE64_PREFIX + imagePath);
        }
        else {
          setImage(FILE_PREFIX + imagePath);
        }
        setRenderComponents(false);
        setRenderCamera(false);
        // TODO: Implement in-app photo preview (integrate React Navigation library)
          // navigation.navigate('PreviewScreen');
        setCapturing(false);
      }
    });
  }

  return (
    <>
      {renderCamera && (
        <OpenCVCameraView
          ref={cameraRef}
          style={styles.camera}
          cameraID={cameraID}
          enableProcessing={enableProcessing}
          saveAsBase64={saveAsBase64}
        />
      )}

      {renderComponents && (
        <>
          <TouchableOpacity style={styles.captureButton} onPress={() => takePic()}>
            {/* Take Picture Button */}
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.filterButton, {backgroundColor: enableProcessing ? 'lime' : 'grey'}]}
            onPress={() => setEnableProcessing(!enableProcessing)}>
            {/* Enable Filter Button */}
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.flipButton, {backgroundColor: enableProcessing ? 'lime' : 'grey'}]}
            onPress={() => setCameraID((cameraID == -1 || cameraID == 99) ? 98 : 99)}>
            {/* Flip Camera Button */}
          </TouchableOpacity>
        </>
      )}
    </>
  );
}

const styles = StyleSheet.create({
  camera: {
    height: '100%',
    width: '100%',
    alignSelf: 'center',
  },
  captureButton: {
    position: 'absolute',
    backgroundColor: 'white',
    bottom: '2%',
    borderRadius: 100,
    alignSelf: 'center',
    height: 75,
    width: 75,
  },
  filterButton: {
    position: 'absolute',
    backgroundColor: 'white',
    bottom: '2%',
    right: '2%',
    borderRadius: 15,
    height: 50,
    width: 50,
  },
  flipButton: {
    position: 'absolute',
    backgroundColor: 'white',
    top: '2%',
    right: '2%',
    borderRadius: 15,
    height: 30,
    width: 30,
  },
});

export default App;