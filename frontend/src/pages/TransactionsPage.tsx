import { useState, useEffect } from 'react';
import { transactionApi, Transaction } from '../api';

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('');
  const [selected, setSelected] = useState<Transaction | null>(null);

  useEffect(() => {
    load();
  }, []);

  const load = () => {
    setLoading(true);
    const params = filter ? { status: filter } : undefined;
    transactionApi.search(params)
      .then((res) => setTransactions(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, [filter]);

  const handleChargeback = async (id: string) => {
    try {
      await transactionApi.chargeback(id);
      load();
      setSelected(null);
    } catch {
      alert('Chargeback failed');
    }
  };

  const statusColors: Record<string, string> = {
    AUTHORIZED: 'bg-yellow-100 text-yellow-800',
    CAPTURED: 'bg-green-100 text-green-800',
    SETTLED: 'bg-blue-100 text-blue-800',
    DECLINED: 'bg-red-100 text-red-800',
    FAILED: 'bg-red-100 text-red-800',
    REFUNDED: 'bg-purple-100 text-purple-800',
    PARTIALLY_REFUNDED: 'bg-orange-100 text-orange-800',
    CHARGEBACK_INITIATED: 'bg-red-100 text-red-800',
  };

  const statuses = ['', 'AUTHORIZED', 'CAPTURED', 'SETTLED', 'DECLINED', 'REFUNDED', 'CHARGEBACK_INITIATED'];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Transactions</h1>
        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
        >
          {statuses.map((s) => (
            <option key={s} value={s}>{s || 'All Statuses'}</option>
          ))}
        </select>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">Loading...</div>
      ) : (
        <div className="bg-white rounded-xl shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Transaction ID</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Amount</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Captured</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Status</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Card</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Date</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {transactions.map((txn) => (
                <tr key={txn.transactionId} className="hover:bg-gray-50">
                  <td className="px-6 py-3 font-mono text-xs">{txn.transactionId}</td>
                  <td className="px-6 py-3">${txn.amount.toFixed(2)}</td>
                  <td className="px-6 py-3">${txn.capturedAmount.toFixed(2)}</td>
                  <td className="px-6 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusColors[txn.status] || 'bg-gray-100'}`}>
                      {txn.status}
                    </span>
                  </td>
                  <td className="px-6 py-3 font-mono text-xs">{txn.maskedCardNumber}</td>
                  <td className="px-6 py-3 text-gray-500 text-xs">{new Date(txn.createdAt).toLocaleString()}</td>
                  <td className="px-6 py-3">
                    <button
                      onClick={() => setSelected(txn)}
                      className="text-indigo-600 hover:text-indigo-800 text-xs font-medium"
                    >
                      Details
                    </button>
                  </td>
                </tr>
              ))}
              {transactions.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-6 py-8 text-center text-gray-400">No transactions found</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Detail Modal */}
      {selected && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg mx-4">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-bold">Transaction Details</h2>
              <button onClick={() => setSelected(null)} className="text-gray-400 hover:text-gray-600 text-xl">&times;</button>
            </div>
            <div className="space-y-2 text-sm">
              {Object.entries({
                'Transaction ID': selected.transactionId,
                'Status': selected.status,
                'Amount': `$${selected.amount.toFixed(2)} ${selected.currency}`,
                'Captured Amount': `$${selected.capturedAmount.toFixed(2)}`,
                'Refunded Amount': `$${selected.refundedAmount.toFixed(2)}`,
                'Card': selected.maskedCardNumber,
                'Auth Code': selected.authorizationCode || 'N/A',
                'Created': new Date(selected.createdAt).toLocaleString(),
              }).map(([key, val]) => (
                <div key={key} className="flex justify-between py-1 border-b border-gray-100">
                  <span className="text-gray-500">{key}</span>
                  <span className="font-medium">{val}</span>
                </div>
              ))}
            </div>
            {['CAPTURED', 'SETTLED'].includes(selected.status) && (
              <button
                onClick={() => handleChargeback(selected.transactionId)}
                className="w-full mt-4 bg-red-600 text-white py-2 rounded-lg hover:bg-red-700 transition-colors text-sm font-medium"
              >
                Initiate Chargeback
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
