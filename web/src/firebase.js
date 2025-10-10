import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";

// Cấu hình Firebase của bạn
const firebaseConfig = {
  apiKey: "AIzaSyC1pd3FLoQV0vNt6uOif72EoQ-GSGb0K6c",
  authDomain: "road-condition-detection-dd79a.firebaseapp.com",
  projectId: "road-condition-detection-dd79a",
  storageBucket: "road-condition-detection-dd79a.firebasestorage.app",
  messagingSenderId: "325376076753",
  appId: "1:325376076753:android:b0cd73650f77971f118ef0"
};

// Khởi tạo Firebase App
const app = initializeApp(firebaseConfig);

// Kết nối Firestore
export const db = getFirestore(app);
