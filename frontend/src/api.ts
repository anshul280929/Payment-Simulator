import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

export interface PaymentRequest {
  merchantId: number;
  amount: number;
  currency: string;
  cardNumber: string;
  expiryMonth: number;
  expiryYear: number;
  cvv: string;
  cardholderName: string;
  description: string;
}

export interface PaymentResponse {
  success: boolean;
  transactionId: string;
  status: string;
  authorizationCode: string;
  message: string;
  maskedCardNumber: string;
}

export interface Transaction {
  transactionId: string;
  merchantId: number;
  amount: number;
  capturedAmount: number;
  refundedAmount: number;
  currency: string;
  status: string;
  maskedCardNumber: string;
  authorizationCode: string;
  createdAt: string;
  updatedAt: string;
}

export interface SettlementBatch {
  id: number;
  batchId: string;
  status: string;
  totalTransactions: number;
  totalAmount: number;
  createdAt: string;
  completedAt: string;
}

export const authApi = {
  login: (data: LoginRequest) => api.post<AuthResponse>('/auth/login', data),
  register: (data: LoginRequest & { role?: string }) => api.post<AuthResponse>('/auth/register', data),
};

export const paymentApi = {
  authorize: (data: PaymentRequest) => api.post<PaymentResponse>('/payments/authorize', data),
  capture: (id: string, amount: number) => api.post<PaymentResponse>(`/payments/${id}/capture`, { amount }),
  refund: (id: string, amount: number) => api.post<PaymentResponse>(`/payments/${id}/refund`, { amount }),
  getStatus: (id: string) => api.get<PaymentResponse>(`/payments/${id}/status`),
};

export const transactionApi = {
  getById: (id: string) => api.get<Transaction>(`/transactions/${id}`),
  search: (params?: { merchantId?: number; status?: string }) =>
    api.get<Transaction[]>('/transactions', { params }),
  chargeback: (id: string) => api.post(`/transactions/${id}/chargeback`),
};

export const settlementApi = {
  process: () => api.post<SettlementBatch>('/settlements/process'),
  getBatches: () => api.get<SettlementBatch[]>('/settlements/batches'),
  getBatch: (batchId: string) => api.get<SettlementBatch>(`/settlements/batches/${batchId}`),
};

export default api;
