import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navItems = [
  { path: '/', label: 'Dashboard' },
  { path: '/checkout', label: 'Checkout Simulator' },
  { path: '/transactions', label: 'Transactions' },
  { path: '/settlements', label: 'Settlements' },
];

export default function Navbar() {
  const { user, logout } = useAuth();
  const location = useLocation();

  return (
    <nav className="bg-indigo-700 shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center space-x-8">
            <span className="text-white font-bold text-lg">Payment Simulator</span>
            <div className="flex space-x-1">
              {navItems.map((item) => (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    location.pathname === item.path
                      ? 'bg-indigo-900 text-white'
                      : 'text-indigo-100 hover:bg-indigo-600'
                  }`}
                >
                  {item.label}
                </Link>
              ))}
            </div>
          </div>
          <div className="flex items-center space-x-4">
            <span className="text-indigo-200 text-sm">
              {user?.username} ({user?.role})
            </span>
            <button
              onClick={logout}
              className="bg-indigo-800 text-white px-3 py-1.5 rounded text-sm hover:bg-indigo-900 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
}
