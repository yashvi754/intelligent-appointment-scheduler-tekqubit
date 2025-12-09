import { useEffect, useState } from 'react';
import { CheckCircle } from 'lucide-react';
import { useAppointment } from '../../context/AppointmentContext';
import { bookAppointment } from '../../services/api';

export default function Step5Confirmation({ onNewAppointment }) {
  const {
    customer,
    vehicle,
    selectedDate,
    slotResponse,
    serviceCatalogId,
    centerId,
    resetFlow,
  } = useAppointment();

  const [bookingStatus, setBookingStatus] = useState('idle');
  const [errorMessage, setErrorMessage] = useState('');
  const [appointmentDetails, setAppointmentDetails] = useState(null);

  useEffect(() => {
    const doBooking = async () => {
      if (bookingStatus !== 'idle') return;
      if (!customer || !vehicle || !slotResponse) return;

      const startTime = selectedDate || slotResponse.earliestSlot;
      if (!startTime) return;

      try {
        setBookingStatus('loading');
        const created = await bookAppointment({
          customerId: customer.customerId,
          vehicleId: vehicle.vehicleId,
          serviceCatalogId,
          centerId,
          startTime,
          emergency: false,
        });
        setAppointmentDetails(created);
        setBookingStatus('success');
      } catch (err) {
        console.error('Error booking appointment', err);
        setErrorMessage('Unable to persist booking. Please try again.');
        setBookingStatus('error');
      }
    };

    doBooking();
  }, [bookingStatus, customer, slotResponse, selectedDate, vehicle]);

  const handleNewAppointment = () => {
    resetFlow();
    onNewAppointment();
  };

  const formatDateTime = (date) => {
    if (!date) return '';
    const dateObj = new Date(date);
    return dateObj.toLocaleString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatDateOnly = (date) => {
    if (!date) return '';
    const dateObj = new Date(date);
    return dateObj.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  // SelectedDate is now a full date-time representing the chosen slot
  const getAppointmentDateTime = () => {
    if (selectedDate) {
      return formatDateTime(selectedDate);
    }
    if (slotResponse?.earliestSlot) {
      return formatDateTime(slotResponse.earliestSlot);
    }
    return 'Not specified';
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white rounded-lg shadow-sm p-6">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-emerald-100 rounded-full mb-4">
            <CheckCircle className="w-10 h-10 text-emerald-600" />
          </div>
          <h2 className="text-2xl font-semibold text-gray-900 mb-2">
            {bookingStatus === 'success'
              ? 'Appointment Booked Successfully!'
              : 'Appointment Summary'}
          </h2>
        </div>

        <div className="bg-gray-50 rounded-lg p-6 border border-gray-200 mb-6">
          <div className="space-y-4">
            <div>
              <p className="text-sm font-medium text-gray-700 mb-1">
                Customer Name:
              </p>
              <p className="text-lg font-semibold text-gray-900">
                {customer?.name}
              </p>
            </div>

            <div>
              <p className="text-sm font-medium text-gray-700 mb-1">Vehicle:</p>

              {appointmentDetails?.status && (
                <div>
                  <p className="text-sm font-medium text-gray-700 mb-1">
                    Status:
                  </p>
                  <p className="text-sm text-gray-700">{appointmentDetails.status}</p>
                </div>
              )}
              <p className="text-lg font-semibold text-gray-900">
                {vehicle?.model}

          {bookingStatus === 'error' && errorMessage && (
            <div className="mb-4 p-3 rounded bg-red-50 border border-red-200 text-sm text-red-700">
              {errorMessage}
            </div>
          )}
              </p>
              <p className="text-sm text-gray-600">VIN: {vehicle?.vin}</p>
            </div>

            <div>
              <p className="text-sm font-medium text-gray-700 mb-1">
                Appointment Date & Time:
              </p>
              <p className="text-lg font-semibold text-gray-900">
                {getAppointmentDateTime()}
              </p>
            </div>

            {slotResponse?.earliestSlot && (
              <div>
                <p className="text-sm font-medium text-gray-700 mb-1">
                  Earliest Available Slot:
                </p>
                <p className="text-sm text-gray-600">
                  {formatDateTime(slotResponse.earliestSlot)}
                </p>
              </div>
            )}
          </div>
        </div>

        <div className="flex justify-center">
          <button
            onClick={handleNewAppointment}
            className="px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors font-medium"
          >
            New Appointment
          </button>
        </div>
      </div>
    </div>
  );
}
