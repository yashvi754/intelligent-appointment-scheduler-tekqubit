import { useEffect, useState } from 'react';
import { User, Box, Wrench, Loader } from 'lucide-react';
import { useAppointment } from '../../context/AppointmentContext';
import { findSlot } from '../../services/api';

export default function Step3IntelligenceCheck({ onNext, onBack }) {
  const { centerId, serviceCatalogId, setSlotResponse } = useAppointment();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [response, setResponse] = useState(null);

  useEffect(() => {
    const fetchSlot = async () => {
      if (!serviceCatalogId || !centerId) {
        setLoading(false);
        setError('Missing service or center selection');
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const data = await findSlot(serviceCatalogId, centerId);
        setResponse(data);
        setSlotResponse(data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to find available slot');
      } finally {
        setLoading(false);
      }
    };

    fetchSlot();
  }, [serviceCatalogId, centerId, setSlotResponse]);

  // Calculate parts status
  const getPartsStatus = () => {
    if (!response) return null;
    const now = new Date();
    const partsArrival = new Date(response.partsArrivalDate);
    return partsArrival <= now ? 'AVAILABLE' : 'OUT_OF_STOCK';
  };

  // Calculate equation details
  const getEquationDetails = () => {
    if (!response) return null;
    const now = new Date();
    const partsArrival = new Date(response.partsArrivalDate);
    const diffTime = partsArrival - now;
    const diffDays = Math.max(0, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
    
    // If parts arrive today or already arrived, no restock needed
    if (diffDays <= 1) {
      return {
        leadTimeDays: 0,
        bufferDays: 0,
      };
    }
    
    // Backend calculates: now + maxLeadTime + 1 day buffer
    // So if diffDays = 3, then leadTimeDays = 2, bufferDays = 1
    return {
      leadTimeDays: Math.max(0, diffDays - 1),
      bufferDays: 1,
    };
  };

  // Determine if blocked before earliest (EU Strategy)
  const isBlockedBeforeEarliest = centerId === 2; // EU Strategy

  const partsStatus = getPartsStatus();
  const equation = getEquationDetails();

  const technicianAvailable = !!response?.earliestSlot && !error;
  const bayAvailable = !!response?.earliestSlot && !error;

  const errorMessage = error?.toLowerCase() || '';
  const technicianUnavailableReason =
    errorMessage.includes('technician') || errorMessage.includes('slots');
  const bayUnavailableReason =
    errorMessage.includes('service bays') || errorMessage.includes('slots');

  const technicianCardStyle = technicianAvailable
    ? 'bg-emerald-50 border-emerald-200'
    : technicianUnavailableReason
    ? 'bg-rose-50 border-rose-200'
    : 'bg-gray-50 border-gray-200';

  const bayCardStyle = bayAvailable
    ? 'bg-emerald-50 border-emerald-200'
    : bayUnavailableReason
    ? 'bg-rose-50 border-rose-200'
    : 'bg-gray-50 border-gray-200';

  const technicianIconColor = technicianAvailable
    ? 'text-emerald-600'
    : technicianUnavailableReason
    ? 'text-rose-600'
    : 'text-gray-400';

  const bayIconColor = bayAvailable
    ? 'text-emerald-600'
    : bayUnavailableReason
    ? 'text-rose-600'
    : 'text-gray-400';

  const partsCardStyle =
    partsStatus === 'AVAILABLE'
      ? 'bg-emerald-50 border-emerald-200'
      : partsStatus === 'OUT_OF_STOCK'
      ? 'bg-rose-50 border-rose-200'
      : 'bg-gray-50 border-gray-200';

  const partsIconColor =
    partsStatus === 'AVAILABLE'
      ? 'text-emerald-600'
      : partsStatus === 'OUT_OF_STOCK'
      ? 'text-rose-600'
      : 'text-gray-400';

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white rounded-lg shadow-sm p-6">
        <h2 className="text-2xl font-semibold text-gray-900 mb-6">
          Step 3: Intelligence Check
        </h2>

        {loading && (
          <div className="flex flex-col items-center justify-center py-12">
            <Loader className="w-8 h-8 text-blue-600 animate-spin mb-4" />
            <p className="text-gray-600">Calculating available resources...</p>
          </div>
        )}

        {error && (
          <div className="mb-6 p-4 bg-rose-50 border border-rose-200 rounded-lg">
            <p className="text-rose-800 font-medium">{error}</p>
          </div>
        )}

        {!loading && (response || error) && (
          <>
            {/* 3-Column Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
              {/* Technician */}
              <div
                className={`p-6 rounded-lg border-2 ${technicianCardStyle}`}
              >
                <div className="flex items-center gap-3 mb-3">
                  <User
                    className={`w-6 h-6 ${technicianIconColor}`}
                  />
                  <h3 className="font-semibold text-gray-900">Technician</h3>
                </div>
                {technicianAvailable ? (
                  <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-emerald-100 text-emerald-800">
                    Available
                  </span>
                ) : technicianUnavailableReason ? (
                  <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-rose-100 text-rose-800">
                    Not Available
                  </span>
                ) : (
                  <span className="text-gray-500 text-sm">Not Available</span>
                )}
              </div>

              {/* Bay */}
              <div
                className={`p-6 rounded-lg border-2 ${bayCardStyle}`}
              >
                <div className="flex items-center gap-3 mb-3">
                  <Box
                    className={`w-6 h-6 ${bayIconColor}`}
                  />
                  <h3 className="font-semibold text-gray-900">Bay</h3>
                </div>
                {bayAvailable ? (
                  <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-emerald-100 text-emerald-800">
                    Available
                  </span>
                ) : bayUnavailableReason ? (
                  <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-rose-100 text-rose-800">
                    Not Available
                  </span>
                ) : (
                  <span className="text-gray-500 text-sm">Not Available</span>
                )}
              </div>

              {/* Parts */}
              <div
                className={`p-6 rounded-lg border-2 ${partsCardStyle}`}
              >
                <div className="flex items-center gap-3 mb-3">
                  <Wrench
                    className={`w-6 h-6 ${partsIconColor}`}
                  />
                  <h3 className="font-semibold text-gray-900">Parts</h3>
                </div>
                {partsStatus === 'AVAILABLE' ? (
                  <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-emerald-100 text-emerald-800">
                    Available
                  </span>
                ) : partsStatus === 'OUT_OF_STOCK' ? (
                  <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-rose-100 text-rose-800">
                    Restocking Needed
                  </span>
                ) : (
                  <span className="text-gray-500 text-sm">Unknown</span>
                )}
              </div>
            </div>

            {/* Timeline Calculation Card */}
            {equation && response.earliestSlot && (
              <div className="bg-gray-50 rounded-lg p-6 mb-6 border border-gray-200">
                <h3 className="font-semibold text-gray-900 mb-4">
                  Timeline Calculation
                </h3>
                <div className="space-y-2">
                  <p className="text-gray-700">
                    <span className="font-medium">{equation.leadTimeDays}</span> Days
                    Restock + <span className="font-medium">{equation.bufferDays}</span>{' '}
                    Day Buffer
                  </p>
                  <p className="text-lg font-semibold text-gray-900">
                    Earliest Availability:{' '}
                    <span className="text-blue-600">
                      {new Date(response.earliestSlot).toLocaleString('en-US', {
                        weekday: 'long',
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </span>
                  </p>
                </div>
              </div>
            )}

            {/* Navigation Buttons */}
            <div className="flex justify-between">
              <button
                onClick={onBack}
                className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors font-medium"
              >
                Back
              </button>
              {response?.earliestSlot && (
                <button
                  onClick={onNext}
                  className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors font-medium"
                >
                  Next: Calendar Selection
                </button>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
