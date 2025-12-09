import { useAppointment } from '../context/AppointmentContext';

const serviceCenters = [
  { id: 1, name: 'Texas Auto Hub (US Strategy)' },
  { id: 2, name: 'London Service Centre (EU Strategy)' },
];

export default function TopBar() {
  const { centerId, setCenterId } = useAppointment();

  return (
    <div className="bg-white border-b border-gray-200 px-6 py-4">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-gray-900">
          Appointment Scheduler
        </h1>
        <div className="flex items-center gap-4">
          <label htmlFor="center-select" className="text-sm font-medium text-gray-700">
            Select Service Center:
          </label>
          <select
            id="center-select"
            value={centerId}
            onChange={(e) => setCenterId(Number(e.target.value))}
            className="px-4 py-2 border border-gray-300 rounded-lg bg-white text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            {serviceCenters.map((center) => (
              <option key={center.id} value={center.id}>
                ID {center.id}: {center.name}
              </option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
}
