import { useState, useEffect } from 'react';
import { Search, User, Car } from 'lucide-react';
import { useAppointment } from '../../context/AppointmentContext';
import { searchCustomers } from '../../services/api';

export default function Step1CustomerLookup({ onNext }) {
  const { customer, setCustomer, vehicle, setVehicle } = useAppointment();
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);

  useEffect(() => {
    const performSearch = async () => {

      setIsSearching(true);
      try {
        const results = await searchCustomers(searchQuery);
        setSearchResults(results || []);
      } catch (error) {
        console.error('Error searching customers', error);
        setSearchResults([]);
      } finally {
        setIsSearching(false);
      }
    };

    const timeoutId = setTimeout(performSearch, 300);
    return () => clearTimeout(timeoutId);
  }, [searchQuery]);

  const handleSelectCustomer = (selectedCustomer) => {
    setCustomer(selectedCustomer);
    setVehicle(null); // Reset vehicle selection
  };

  const handleSelectVehicle = (selectedVehicle) => {
    setVehicle(selectedVehicle);
  };

  const handleNext = () => {
    if (customer && vehicle) {
      onNext();
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white rounded-lg shadow-sm p-6">
        <h2 className="text-2xl font-semibold text-gray-900 mb-6">
          Step 1: Customer Lookup
        </h2>

        {/* Search Input */}
        <div className="relative mb-6">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <input
            type="text"
            placeholder="Search by customer name or phone..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        {/* Search Results */}
        {isSearching && (
          <div className="text-center py-2 text-gray-500">Searching...</div>
        )}

        {!isSearching && !customer && searchQuery.length >= 1 && (
          <div className="mb-4">
            {searchResults.length > 0 ? (
              <ul className="border border-gray-200 rounded-lg divide-y divide-gray-100 bg-white">
                {searchResults.map((c) => (
                  <li
                    key={c.customerId}
                    onClick={() => handleSelectCustomer(c)}
                    className="px-4 py-2 hover:bg-gray-50 cursor-pointer flex items-center gap-2"
                  >
                    <User className="w-4 h-4 text-gray-400" />
                    <span className="flex-1">{c.name} | {c.phone}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <div className="text-sm text-gray-500">No customers found</div>
            )}
          </div>
        )}

        {/* Selected Customer Card */}
        {customer && (
          <div className="mb-6">
            <div className="bg-gray-50 rounded-lg p-6 border border-gray-200">
              <div className="flex items-start gap-4 mb-4">
                <div className="bg-blue-100 rounded-full p-3">
                  <User className="w-6 h-6 text-blue-600" />
                </div>
                <div className="flex-1">
                  <h3 className="text-xl font-semibold text-gray-900">
                    {customer.name}
                  </h3>
                  <p className="text-gray-600">{customer.phone}</p>
                  <div className="mt-2">
                    <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-emerald-100 text-emerald-800">
                      Loyalty Points: {customer.loyaltyScore}
                    </span>
                  </div>
                </div>
                <button
                  onClick={() => {
                    setCustomer(null);
                    setVehicle(null);
                  }}
                  className="text-gray-400 hover:text-gray-600"
                >
                  ✕
                </button>
              </div>

              {/* Vehicles List */}
              <div className="mt-4">
                <h4 className="text-sm font-medium text-gray-700 mb-3">
                  Select a Vehicle:
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  {(customer.vehicles || []).map((v) => (
                    <div
                      key={v.vehicleId}
                      onClick={() => handleSelectVehicle(v)}
                      className={`p-4 border-2 rounded-lg cursor-pointer transition-all ${
                        vehicle?.vehicleId === v.vehicleId
                          ? 'border-blue-500 bg-blue-50'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <Car className="w-5 h-5 text-gray-400" />
                        <div>
                          <div className="font-medium text-gray-900">
                            {v.model}
                          </div>
                          <div className="text-sm text-gray-500">VIN: {v.vin}</div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Next Button */}
        {customer && vehicle && (
          <div className="flex justify-end">
            <button
              onClick={handleNext}
              className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors font-medium"
            >
              Next: Diagnosis
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
