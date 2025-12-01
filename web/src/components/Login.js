import React, { useState } from 'react';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../firebase';
import './Login.css';

export default function Login({ onSwitchToRegister }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await signInWithEmailAndPassword(auth, email, password);
      // User sẽ được tự động cập nhật qua onAuthStateChanged
    } catch (err) {
      console.error('Lỗi đăng nhập:', err);
      switch (err.code) {
        case 'auth/user-not-found':
          setError('Tài khoản không tồn tại');
          break;
        case 'auth/wrong-password':
          setError('Mật khẩu không chính xác');
          break;
        case 'auth/invalid-email':
          setError('Email không hợp lệ');
          break;
        case 'auth/invalid-credential':
          setError('Tài khoản hoặc mật khẩu không đúng');
          break;
        default:
          setError('Đăng nhập thất bại. Vui lòng thử lại.');
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
          <h2>Đăng nhập</h2>
          <p>Đăng nhập để tiếp tục sử dụng hệ thống</p>
        </div>

        <form onSubmit={handleLogin} className="auth-form">
          {error && (
            <div className="error-message">
              <span className="error-icon">⚠️</span>
              {error}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Nhập email của bạn"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Mật khẩu</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Nhập mật khẩu"
              required
              disabled={loading}
            />
          </div>

          <button 
            type="submit" 
            className="auth-button"
            disabled={loading}
          >
            {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>

          <div className="auth-footer">
            <p>
              Chưa có tài khoản?{' '}
              <button
                type="button"
                onClick={onSwitchToRegister}
                className="switch-auth-button"
                disabled={loading}
              >
                Đăng ký ngay
              </button>
            </p>
          </div>
        </form>
      </div>
    </div>
  );
}

