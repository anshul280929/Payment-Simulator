import { useState, useEffect } from 'react';
import { transactionApi, Transaction } from '../api';

export default function DashboardPage() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    transactionApi.search()
      .then((res) => setTransactions(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const stats = {
    total: transactions.length,
    authorized: transactions.filter((t) => t.status === 'AUTHORIZED').length,
    captured: transactions.filter((t) => t.status === 'CAPTURED').length,
    declined: transactions.filter((t) => t.status === 'DECLINED').length,
    totalAmount: transactions.reduce((sum, t) => sum + t.amount, 0),
    capturedAmount: transactions
      .filter((t) => ['CAPTURED', 'SETTLED'].includes(t.status))
      .reduce((sum, t) => sum + t.capturedAmount, 0),
  };

  const statCards = [
    { label: 'Total Transactions', value: stats.total, color: 'bg-blue-500' },
    { label: 'Authorized', value: stats.authorized, color: 'bg-yellow-500' },
    { label: 'Captured', value: stats.captured, color: 'bg-green-500' },
    { label: 'Declined', value: stats.declined, color: 'bg-red-500' },
    { label: 'Total Amount', value: `$${stats.totalAmount.toFixed(2)}`, color: 'bg-indigo-500' },
    { label: 'Captured Amount', value: `$${stats.capturedAmount.toFixed(2)}`, color: 'bg-emerald-500' },
  ];

  if (loading) {
    return <div className="text-center py-12 text-gray-500">Loading dashboard...</div>;
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        {statCards.map((card) => (
          <div key={card.label} className="bg-white rounded-xl shadow p-6">
            <div className="flex items-center space-x-3">
              <div className={`w-3 h-3 rounded-full ${card.color}`}></div>
              <span className="text-sm text-gray-500">{card.label}</span>
            </div>
            <p className="text-3xl font-bold mt-2 text-gray-800">{card.value}</p>
          </div>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-800">Recent Transactions</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Transaction ID</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Amount</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Status</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Card</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {transactions.slice(0, 10).map((txn) => (
                <tr key={txn.transactionId} className="hover:bg-gray-50">
                  <td className="px-6 py-3 font-mono text-xs">{txn.transactionId}</td>
                  <td className="px-6 py-3">${txn.amount.toFixed(2)} {txn.currency}</td>
                  <td className="px-6 py-3">
                    <StatusBadge status={txn.status} />
                  </td>
                  <td className="px-6 py-3 font-mono text-xs">{txn.maskedCardNumber}</td>
                  <td className="px-6 py-3 text-gray-500">{new Date(txn.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
              {transactions.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-gray-400">No transactions yet</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function StatusBadge({ status }: { status: string }) {
  const colors: Record<string, string> = {
    AUTHORIZED: 'bg-yellow-100 text-yellow-800',
    CAPTURED: 'bg-green-100 text-green-800',
    SETTLED: 'bg-blue-100 text-blue-800',
    DECLINED: 'bg-red-100 text-red-800',
    FAILED: 'bg-red-100 text-red-800',
    REFUNDED: 'bg-purple-100 text-purple-800',
    PARTIALLY_REFUNDED: 'bg-orange-100 text-orange-800',
    CHARGEBACK_INITIATED: 'bg-red-100 text-red-800',
    PENDING: 'bg-gray-100 text-gray-800',
  };
  return (
    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${colors[status] || 'bg-gray-100 text-gray-800'}`}>
      {status}
    </span>
  );
}
