import { useState, useEffect } from 'react';
import { settlementApi, SettlementBatch } from '../api';

export default function SettlementsPage() {
  const [batches, setBatches] = useState<SettlementBatch[]>([]);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);

  const load = () => {
    setLoading(true);
    settlementApi.getBatches()
      .then((res) => setBatches(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const handleProcess = async () => {
    setProcessing(true);
    try {
      await settlementApi.process();
      load();
    } catch {
      alert('No pending entries to settle');
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Settlements</h1>
        <button
          onClick={handleProcess}
          disabled={processing}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors text-sm font-medium"
        >
          {processing ? 'Processing...' : 'Trigger Settlement'}
        </button>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">Loading...</div>
      ) : (
        <div className="bg-white rounded-xl shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Batch ID</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Status</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Transactions</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Total Amount</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Created</th>
                <th className="text-left px-6 py-3 text-gray-500 font-medium">Completed</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {batches.map((batch) => (
                <tr key={batch.batchId} className="hover:bg-gray-50">
                  <td className="px-6 py-3 font-mono text-xs">{batch.batchId}</td>
                  <td className="px-6 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                      batch.status === 'COMPLETED' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                    }`}>
                      {batch.status}
                    </span>
                  </td>
                  <td className="px-6 py-3">{batch.totalTransactions}</td>
                  <td className="px-6 py-3 font-medium">${batch.totalAmount.toFixed(2)}</td>
                  <td className="px-6 py-3 text-gray-500 text-xs">{new Date(batch.createdAt).toLocaleString()}</td>
                  <td className="px-6 py-3 text-gray-500 text-xs">
                    {batch.completedAt ? new Date(batch.completedAt).toLocaleString() : '—'}
                  </td>
                </tr>
              ))}
              {batches.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-gray-400">No settlement batches yet</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
