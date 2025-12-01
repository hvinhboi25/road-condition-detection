import React, { useState } from 'react';
import { createUserWithEmailAndPassword, updateProfile } from 'firebase/auth';
import { doc, setDoc } from 'firebase/firestore';
import { auth, db } from '../firebase';
import './Login.css';

export default function Register({ onSwitchToLogin }) {
  const [formData, setFormData] = useState({
    name: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');

    // Validation
    if (formData.password !== formData.confirmPassword) {
      setError('Mật khẩu xác nhận không khớp');
      return;
    }

    if (formData.password.length < 6) {
      setError('Mật khẩu phải có ít nhất 6 ký tự');
      return;
    }

    if (!formData.name.trim()) {
      setError('Vui lòng nhập tên của bạn');
      return;
    }

    if (!formData.username.trim()) {
      setError('Vui lòng nhập tên tài khoản');
      return;
    }

    setLoading(true);

    try {
      // Tạo tài khoản Firebase Auth
      const userCredential = await createUserWithEmailAndPassword(
        auth, 
        formData.email, 
        formData.password
      );

      const user = userCredential.user;

      // Cập nhật profile với tên hiển thị
      await updateProfile(user, {
        displayName: formData.name
      });

      // Lưu thông tin user vào Firestore
      await setDoc(doc(db, 'users', user.uid), {
        uid: user.uid,
        name: formData.name,
        username: formData.username,
        email: formData.email,
        createdAt: new Date().toISOString(),
        role: 'user'
      });

      // User sẽ được tự động đăng nhập qua onAuthStateChanged
    } catch (err) {
      console.error('Lỗi đăng ký:', err);
      console.error('Error code:', err.code);
      console.error('Error message:', err.message);
      
      switch (err.code) {
        case 'auth/email-already-in-use':
          setError('Email đã được sử dụng');
          break;
        case 'auth/invalid-email':
          setError('Email không hợp lệ');
          break;
        case 'auth/weak-password':
          setError('Mật khẩu quá yếu (tối thiểu 6 ký tự)');
          break;
        case 'auth/operation-not-allowed':
          setError('Đăng ký chưa được kích hoạt. Vui lòng liên hệ admin.');
          break;
        case 'auth/network-request-failed':
          setError('Lỗi kết nối mạng. Vui lòng kiểm tra internet.');
          break;
        case 'permission-denied':
          setError('Lỗi phân quyền Firestore. Vui lòng liên hệ admin.');
          break;
        default:
          setError(`Đăng ký thất bại: ${err.message || 'Lỗi không xác định'}`);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>🛣️ Road Condition Detection</h1>
          <h2>Đăng ký tài khoản</h2>
          <p>Tạo tài khoản mới để sử dụng hệ thống</p>
        </div>

        <form onSubmit={handleRegister} className="auth-form">
          {error && (
            <div className="error-message">
              <span className="error-icon">⚠️</span>
              {error}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="name">Tên đầy đủ</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Nguyễn Văn A"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="username">Tên tài khoản</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="nguyenvana"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="example@email.com"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Mật khẩu</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Tối thiểu 6 ký tự"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">Xác nhận mật khẩu</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="Nhập lại mật khẩu"
              required
              disabled={loading}
            />
          </div>

          <button 
            type="submit" 
            className="auth-button"
            disabled={loading}
          >
            {loading ? 'Đang đăng ký...' : 'Đăng ký'}
          </button>

          <div className="auth-footer">
            <p>
              Đã có tài khoản?{' '}
              <button
                type="button"
                onClick={onSwitchToLogin}
                className="switch-auth-button"
                disabled={loading}
              >
                Đăng nhập ngay
              </button>
            </p>
          </div>
        </form>
      </div>
    </div>
  );
}

