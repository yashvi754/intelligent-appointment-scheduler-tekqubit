import { useState, useEffect } from 'react';
import { AlertTriangle } from 'lucide-react';
import { useAppointment } from '../../context/AppointmentContext';

export default function Step4CalendarSelection({ onNext, onBack }) {
  const { centerId, slotResponse, selectedDate, setSelectedDate } =
    useAppointment();

  // Determine if blocked before earliest (EU Strategy)
  const isBlockedBeforeEarliest = centerId === 2; // EU Strategy

  const earliestSlot = slotResponse?.earliestSlot
    ? new Date(slotResponse.earliestSlot)
    : null;

  // Track which day's slots are being shown
  const [currentDate, setCurrentDate] = useState(() => {
    if (selectedDate) return new Date(selectedDate);
    if (earliestSlot) return earliestSlot;
    return null;
  });

  const displayDate = currentDate || earliestSlot;

  // Track current time to control which slots are shown/enabled
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    const intervalId = setInterval(() => {
      setNow(new Date());
    }, 60_000); // update every minute

    return () => clearInterval(intervalId);
  }, []);

  const changeDay = (delta) => {
    if (!displayDate) return;
    const next = new Date(displayDate);
    next.setDate(next.getDate() + delta);
    setCurrentDate(next);
  };

  // Generate 30-minute time slots from 9:00 AM to 6:00 PM
  const getTimeSlotsForDay = () => {
    if (!displayDate) return [];

    const baseDate = new Date(
      displayDate.getFullYear(),
      displayDate.getMonth(),
      displayDate.getDate(),
      9,
      0,
      0,
      0
    );

    const slots = [];
    const slotCount = 18; // 9 hours * 2 slots/hour
    const slotDurationMs = 30 * 60 * 1000;

    for (let i = 0; i < slotCount; i++) {
      slots.push(new Date(baseDate.getTime() + i * slotDurationMs));
    }

    return slots;
  };

  const slots = getTimeSlotsForDay();

  const isSlotDisabled = (slot) => {
    let disabled = false;

    // Disable slots in the past relative to current date/time
    if (now && displayDate) {
      const today = new Date(
        now.getFullYear(),
        now.getMonth(),
        now.getDate()
      );
      const displayDay = new Date(
        displayDate.getFullYear(),
        displayDate.getMonth(),
        displayDate.getDate()
      );

      if (displayDay.getTime() < today.getTime()) {
        disabled = true;
      } else if (
        displayDay.getTime() === today.getTime() &&
        slot.getTime() <= now.getTime()
      ) {
        disabled = true;
      }
    }

    // Apply earliest-slot rules for EU strategy
    if (earliestSlot && isBlockedBeforeEarliest) {
      if (slot.getTime() < earliestSlot.getTime()) {
        disabled = true;
      }
    }

    return disabled;
  };

  const isSlotSelected = (slot) => {
    if (!slot || !selectedDate) return false;
    return slot.getTime() === new Date(selectedDate).getTime();
  };

  const handleSlotClick = (slot) => {
    if (!slot || isSlotDisabled(slot)) return;
    setSelectedDate(slot);
  };

  const handleNext = () => {
    if (selectedDate) {
      onNext();
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white rounded-lg shadow-sm p-6">
        <h2 className="text-2xl font-semibold text-gray-900 mb-6">
          Step 4: Time Slot Selection
        </h2>
        {/* Time Slot Grid */}
        <div className="mb-6">
          <div className="flex items-center justify-between mb-3">
            <button
              type="button"
              onClick={() => changeDay(-1)}
              className="px-3 py-1 rounded-lg border border-gray-300 text-sm text-gray-700 hover:bg-gray-50"
            >
              &lt;
            </button>
            <p className="text-sm text-gray-700">
              Showing available slots for{' '}
              {displayDate?.toLocaleDateString('en-US', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </p>
            <button
              type="button"
              onClick={() => changeDay(1)}
              className="px-3 py-1 rounded-lg border border-gray-300 text-sm text-gray-700 hover:bg-gray-50"
            >
              &gt;
            </button>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
            {slots.map((slot) => {
              const disabled = isSlotDisabled(slot);
              const selected = isSlotSelected(slot);

              return (
                <button
                  key={slot.toISOString()}
                  type="button"
                  onClick={() => handleSlotClick(slot)}
                  disabled={disabled}
                  className={`px-4 py-2 rounded-lg text-sm font-medium border transition-colors ${
                    disabled
                      ? 'bg-gray-100 text-gray-400 border-gray-200 cursor-not-allowed'
                      : selected
                      ? 'bg-blue-600 text-white border-blue-600'
                      : 'bg-white text-gray-900 border-gray-200 hover:bg-gray-50'
                  }`}
                  title={
                    disabled
                      ? 'Blocked due to parts and resource availability'
                      : slot.toLocaleTimeString('en-US', {
                          hour: '2-digit',
                          minute: '2-digit',
                        })
                  }
                >
                  {slot.toLocaleTimeString('en-US', {
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </button>
              );
            })}
          </div>
        </div>

        {/* Warning for US Strategy */}
        {!isBlockedBeforeEarliest && earliestSlot && selectedDate &&
          new Date(selectedDate).getTime() < earliestSlot.getTime() && (
          <div className="mb-6 flex items-start gap-3 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
            <AlertTriangle className="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5" />
            <div>
              <p className="text-sm font-medium text-yellow-900">
                Risk of Delay
              </p>
              <p className="text-sm text-yellow-700">
                Selected date may be before parts arrive. Appointment may be
                delayed.
              </p>
            </div>
          </div>
        )}

        {/* Selected Date Display */}
        {selectedDate && (
          <div className="mb-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
            <p className="text-sm font-medium text-gray-700 mb-1">
              Selected Time Slot:
            </p>
            <p className="text-lg font-semibold text-gray-900">
              {new Date(selectedDate).toLocaleString('en-US', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
              })}
            </p>
            {earliestSlot && (
              <p className="text-sm text-gray-600 mt-2">
                Earliest available slot:{' '}
                {earliestSlot.toLocaleTimeString('en-US', {
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </p>
            )}
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
          {selectedDate && (
            <button
              onClick={handleNext}
              className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors font-medium"
            >
              Next: Confirmation
            </button>
          )}
        </div>

        {/* Current Date & Time (bottom-right) */}
        <div className="mt-4 flex justify-end">
          <p className="text-xs text-gray-500">
            Current date & time:{' '}
            {now.toLocaleString('en-US', {
              weekday: 'short',
              year: 'numeric',
              month: 'short',
              day: 'numeric',
              hour: '2-digit',
              minute: '2-digit',
            })}
          </p>
        </div>
      </div>
    </div>
  );
}
