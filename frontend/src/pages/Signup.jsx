import React, { useState, useContext, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import {
  Shield,
  Lock,
  Mail,
  AlertCircle,
  CheckCircle2,
  Loader2,
  Eye,
  EyeOff,
  Info,
} from 'lucide-react';
import api from '../services/api';
import './Signup.css';

// Password strength validation — mirrors backend rules exactly
const validatePassword = (pw) => {
  if (!pw) return 'Password is required.';
  if (pw.length < 8) return 'Password must be at least 8 characters long.';
  if (!/[A-Z]/.test(pw)) return 'Password must contain at least one uppercase letter.';
  if (!/[a-z]/.test(pw)) return 'Password must contain at least one lowercase letter.';
  if (!/\d/.test(pw)) return 'Password must contain at least one digit.';
  return null;
};

const Signup = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { isAuthenticated } = useContext(AuthContext);
  const navigate = useNavigate();

  // Redirect authenticated users away from signup
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Client-side validation
    if (!email || !email.trim()) {
      setError('Email is required.');
      return;
    }

    const pwError = validatePassword(password);
    if (pwError) {
      setError(pwError);
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setIsLoading(true);

    try {
      const response = await api.post('/auth/signup', {
        email: email.trim().toLowerCase(),
        password,
      });

      setSuccess(
        response.data?.message ||
          'Registration successful! Redirecting to login…'
      );

      // Redirect to login after a brief success pause
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      const serverMessage =
        typeof err.response?.data === 'string'
          ? err.response.data
          : err.response?.data?.message;

      if (err.response?.status === 409) {
        setError(serverMessage || 'An account with this email already exists.');
      } else if (err.response?.status === 400) {
        setError(serverMessage || 'Please check your input and try again.');
      } else {
        setError('Registration failed. Please try again later.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="signup-container">
      <div className="signup-card">
        {/* ── Header ── */}
        <div className="signup-header">
          <div className="logo-container">
            <Shield className="logo-icon" size={40} />
          </div>
          <h2>Create Account</h2>
          <p>Military Asset Management System</p>
        </div>

        {/* ── Role notice — no dropdown exposed ── */}
        <div className="role-notice">
          <Info size={16} />
          <span>
            Public accounts are registered as <strong>Logistics Officer</strong>.
            Contact your administrator for elevated access.
          </span>
        </div>

        {/* ── Error alert ── */}
        {error && (
          <div className="signup-error-alert">
            <AlertCircle size={18} style={{ flexShrink: 0, marginTop: 2 }} />
            <span>{error}</span>
          </div>
        )}

        {/* ── Success alert ── */}
        {success && (
          <div className="signup-success-alert">
            <CheckCircle2 size={18} style={{ flexShrink: 0, marginTop: 2 }} />
            <span>{success}</span>
          </div>
        )}

        {/* ── Form ── */}
        <form onSubmit={handleSubmit} className="login-form" noValidate>
          {/* Email */}
          <div className="form-group">
            <label htmlFor="signup-email">Email Address</label>
            <div className="input-with-icon">
              <Mail className="input-icon" size={18} />
              <input
                id="signup-email"
                type="email"
                placeholder="Enter your email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={isLoading || !!success}
                required
                autoComplete="email"
              />
            </div>
          </div>

          {/* Password */}
          <div className="form-group">
            <label htmlFor="signup-password">Password</label>
            <div className="password-toggle-wrapper">
              <Lock
                size={18}
                style={{
                  position: 'absolute',
                  left: '1rem',
                  color: 'var(--text-muted)',
                  zIndex: 1,
                }}
              />
              <input
                id="signup-password"
                type={showPassword ? 'text' : 'password'}
                placeholder="Min 8 chars, upper, lower, digit"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={isLoading || !!success}
                required
                autoComplete="new-password"
              />
              <button
                type="button"
                className="pw-toggle-btn"
                onClick={() => setShowPassword((v) => !v)}
                tabIndex={-1}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          {/* Confirm Password */}
          <div className="form-group">
            <label htmlFor="signup-confirm">Confirm Password</label>
            <div className="password-toggle-wrapper">
              <Lock
                size={18}
                style={{
                  position: 'absolute',
                  left: '1rem',
                  color: 'var(--text-muted)',
                  zIndex: 1,
                }}
              />
              <input
                id="signup-confirm"
                type={showConfirm ? 'text' : 'password'}
                placeholder="Re-enter your password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                disabled={isLoading || !!success}
                required
                autoComplete="new-password"
              />
              <button
                type="button"
                className="pw-toggle-btn"
                onClick={() => setShowConfirm((v) => !v)}
                tabIndex={-1}
                aria-label={showConfirm ? 'Hide password' : 'Show password'}
              >
                {showConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          {/* Submit */}
          <button
            id="signup-submit"
            type="submit"
            className="signup-btn"
            disabled={isLoading || !!success}
          >
            {isLoading ? (
              <>
                <Loader2 className="spinner" size={18} />
                <span>Creating Account…</span>
              </>
            ) : (
              'Create Account'
            )}
          </button>
        </form>

        {/* ── Footer ── */}
        <div className="signup-footer">
          <p>
            Already have an account?
            <Link to="/login">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Signup;
