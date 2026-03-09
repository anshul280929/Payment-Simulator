import { useState, FormEvent } from 'react';
import { paymentApi, PaymentRequest, PaymentResponse } from '../api';

const testCards = [
  { label: 'Approved Card', number: '4111111111111234' },
  { label: 'Insufficient Funds', number: '4111111111110000' },
  { label: 'Expired Card', number: '4111111111111111' },
  { label: 'Timeout Card', number: '4111111111119999' },
];

export default function CheckoutPage() {
  const [form, setForm] = useState<PaymentRequest>({
    merchantId: 1,
    amount: 99.99,
    currency: 'USD',
    cardNumber: '4111111111111234',
    expiryMonth: 12,
    expiryYear: 2026,
    cvv: '123',
    cardholderName: 'John Doe',
    description: 'Test payment',
  });

  const [result, setResult] = useState<PaymentResponse | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [captureLoading, setCaptureLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: ['amount', 'expiryMonth', 'expiryYear', 'merchantId'].includes(name)
        ? Number(value)
        : value,
    }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setResult(null);
    setLoading(true);
    try {
      const res = await paymentApi.authorize(form);
      setResult(res.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Payment failed');
    } finally {
      setLoading(false);
    }
  };

  const handleCapture = async () => {
    if (!result?.transactionId) return;
    setCaptureLoading(true);
    try {
      const res = await paymentApi.capture(result.transactionId, form.amount);
      setResult(res.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Capture failed');
    } finally {
      setCaptureLoading(false);
    }
  };

  const handleRefund = async () => {
    if (!result?.transactionId) return;
    try {
      const res = await paymentApi.refund(result.transactionId, form.amount);
      setResult(res.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Refund failed');
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Checkout Simulator</h1>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Payment Form */}
        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-lg font-semibold mb-4">Payment Details</h2>

          <div className="mb-4">
            <label className="block text-xs font-medium text-gray-500 mb-2">Quick Fill Test Card</label>
            <div className="flex flex-wrap gap-2">
              {testCards.map((card) => (
                <button
                  key={card.number}
                  type="button"
                  onClick={() => setForm((prev) => ({ ...prev, cardNumber: card.number }))}
                  className={`px-2 py-1 rounded text-xs border transition-colors ${
                    form.cardNumber === card.number
                      ? 'border-indigo-500 bg-indigo-50 text-indigo-700'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  {card.label}
                </button>
              ))}
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Amount</label>
                <input
                  type="number"
                  name="amount"
                  value={form.amount}
                  onChange={handleChange}
                  step="0.01"
                  min="0.01"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Currency</label>
                <select
                  name="currency"
                  value={form.currency}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="USD">USD</option>
                  <option value="EUR">EUR</option>
                  <option value="GBP">GBP</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Card Number</label>
              <input
                type="text"
                name="cardNumber"
                value={form.cardNumber}
                onChange={handleChange}
                maxLength={16}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm font-mono focus:ring-2 focus:ring-indigo-500"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Cardholder Name</label>
              <input
                type="text"
                name="cardholderName"
                value={form.cardholderName}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
                required
              />
            </div>

            <div className="grid grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Exp Month</label>
                <input
                  type="number"
                  name="expiryMonth"
                  value={form.expiryMonth}
                  onChange={handleChange}
                  min="1"
                  max="12"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Exp Year</label>
                <input
                  type="number"
                  name="expiryYear"
                  value={form.expiryYear}
                  onChange={handleChange}
                  min="2024"
                  max="2035"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">CVV</label>
                <input
                  type="password"
                  name="cvv"
                  value={form.cvv}
                  onChange={handleChange}
                  maxLength={4}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Description</label>
              <input
                type="text"
                name="description"
                value={form.description}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-indigo-600 text-white py-2.5 rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors font-medium"
            >
              {loading ? 'Processing...' : 'Authorize Payment'}
            </button>
          </form>
        </div>

        {/* Result */}
        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-lg font-semibold mb-4">Result</h2>

          {error && (
            <div className="bg-red-50 text-red-700 p-4 rounded-lg mb-4">{error}</div>
          )}

          {result ? (
            <div className="space-y-4">
              <div className={`p-4 rounded-lg ${
                result.success ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'
              }`}>
                <p className={`text-lg font-bold ${result.success ? 'text-green-700' : 'text-red-700'}`}>
                  {result.success ? 'Payment Authorized' : 'Payment Declined'}
                </p>
                <p className="text-sm text-gray-600 mt-1">{result.message}</p>
              </div>

              <div className="space-y-2 text-sm">
                <div className="flex justify-between py-1 border-b border-gray-100">
                  <span className="text-gray-500">Transaction ID</span>
                  <span className="font-mono text-xs">{result.transactionId}</span>
                </div>
                <div className="flex justify-between py-1 border-b border-gray-100">
                  <span className="text-gray-500">Status</span>
                  <span className="font-medium">{result.status}</span>
                </div>
                <div className="flex justify-between py-1 border-b border-gray-100">
                  <span className="text-gray-500">Auth Code</span>
                  <span className="font-mono text-xs">{result.authorizationCode || 'N/A'}</span>
                </div>
                <div className="flex justify-between py-1 border-b border-gray-100">
                  <span className="text-gray-500">Card</span>
                  <span className="font-mono text-xs">{result.maskedCardNumber}</span>
                </div>
              </div>

              {result.success && result.status === 'AUTHORIZED' && (
                <div className="flex gap-3 mt-4">
                  <button
                    onClick={handleCapture}
                    disabled={captureLoading}
                    className="flex-1 bg-green-600 text-white py-2 rounded-lg hover:bg-green-700 disabled:opacity-50 transition-colors text-sm font-medium"
                  >
                    {captureLoading ? 'Capturing...' : 'Capture'}
                  </button>
                </div>
              )}

              {result.success && result.status === 'CAPTURED' && (
                <button
                  onClick={handleRefund}
                  className="w-full bg-orange-500 text-white py-2 rounded-lg hover:bg-orange-600 transition-colors text-sm font-medium"
                >
                  Refund
                </button>
              )}
            </div>
          ) : (
            <p className="text-gray-400 text-center py-12">Submit a payment to see results</p>
          )}
        </div>
      </div>
    </div>
  );
}
