import { useState } from 'react';
import { useAppointment } from '../../context/AppointmentContext';

// Keyword to Service ID mapping (matching DataSeeder: 6 services)
const keywordToServiceMap = {
  // 1 - Squeaking Brakes
  brakes: 1,
  brake: 1,
  squeaking: 1,

  // 2 - Engine Light
  engine: 2,
  'check engine': 2,
  'engine light': 2,

  // 3 - Overheating
  overheating: 3,
  overheat: 3,
  'engine hot': 3,
  coolant: 3,

  // 4 - Oil Change
  oil: 4,
  'oil change': 4,

  // 5 - Dead Battery
  battery: 5,
  'dead battery': 5,
  "won't start": 5,

  // 6 - Tire Issues
  tire: 6,
  tires: 6,
  flat: 6,
};

// Service metadata, kept in sync with backend DataSeeder durations (minutes)
const serviceDetails = {
  1: { name: 'Squeaking Brakes', durationMinutes: 120 },
  2: { name: 'Engine Light', durationMinutes: 180 },
  3: { name: 'Overheating', durationMinutes: 60 },
  4: { name: 'Oil Change', durationMinutes: 60 },
  5: { name: 'Dead Battery', durationMinutes: 60 },
  6: { name: 'Tire Issues', durationMinutes: 90 },
};

const formatDurationHours = (minutes) => {
  const hours = minutes / 60;
  return hours % 1 === 0 ? `${hours} hours` : `${hours.toFixed(1)} hours`;
};

export default function Step2Diagnosis({ onNext, onBack }) {
  const { serviceCatalogId, setServiceCatalogId } = useAppointment();
  const [description, setDescription] = useState('');
  const [detectedService, setDetectedService] = useState(null);

  const handleDescriptionChange = (e) => {
    const text = e.target.value.toLowerCase();
    setDescription(e.target.value);

    // Detect service from keywords
    for (const [keyword, serviceId] of Object.entries(keywordToServiceMap)) {
      if (text.includes(keyword)) {
        setDetectedService(serviceId);
        setServiceCatalogId(serviceId);
        return;
      }
    }

    // If no keyword matches, clear selection
    if (text.length === 0) {
      setDetectedService(null);
      setServiceCatalogId(null);
    }
  };

  const handleServiceSelect = (serviceId) => {
    setDetectedService(serviceId);
    setServiceCatalogId(serviceId);
  };

  const handleNext = () => {
    if (serviceCatalogId) {
      onNext();
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white rounded-lg shadow-sm p-6">
        <h2 className="text-2xl font-semibold text-gray-900 mb-6">
          Step 2: Diagnosis
        </h2>

        <div className="mb-6">
          <label
            htmlFor="description"
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            Describe the issue...
          </label>
          <textarea
            id="description"
            value={description}
            onChange={handleDescriptionChange}
            placeholder="e.g., My brakes are squeaking, or I need an oil change..."
            rows={6}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
          />
        </div>

        {/* Detected Service */}
        {detectedService && (
          <div className="mb-6">
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-blue-900 mb-1">
                    Detected Service:
                  </p>
                  <p className="text-lg font-semibold text-blue-900">
                    {serviceDetails[detectedService]?.name}
                  </p>
                  {serviceDetails[detectedService] && (
                    <p className="text-sm text-blue-800 mt-1">
                      Duration: {formatDurationHours(serviceDetails[detectedService].durationMinutes)}
                    </p>
                  )}
                </div>
                <button
                  onClick={() => {
                    setDetectedService(null);
                    setServiceCatalogId(null);
                  }}
                  className="text-blue-600 hover:text-blue-800"
                >
                  Change
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Manual Service Selection */}
        {!detectedService && description.length > 0 && (
          <div className="mb-6">
            <p className="text-sm font-medium text-gray-700 mb-3">
              Or select a service manually:
            </p>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
              {Object.entries(serviceDetails).map(([id, details]) => (
                <button
                  key={id}
                  onClick={() => handleServiceSelect(Number(id))}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 hover:border-blue-500 transition-colors text-sm font-medium text-gray-700"
                >
                  <span className="block">{details.name}</span>
                  <span className="block text-xs text-gray-500">
                    {formatDurationHours(details.durationMinutes)}
                  </span>
                </button>
              ))}
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
          {serviceCatalogId && (
            <button
              onClick={handleNext}
              className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors font-medium"
            >
              Next: Intelligence Check
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
