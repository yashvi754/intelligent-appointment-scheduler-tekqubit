import { createContext, useContext, useState } from 'react';

const AppointmentContext = createContext();

export const useAppointment = () => {
  const context = useContext(AppointmentContext);
  if (!context) {
    throw new Error('useAppointment must be used within AppointmentProvider');
  }
  return context;
};

export const AppointmentProvider = ({ children }) => {
  const [centerId, setCenterId] = useState(1); // Default to Texas Auto Hub
  const [customer, setCustomer] = useState(null);
  const [vehicle, setVehicle] = useState(null);
  const [serviceCatalogId, setServiceCatalogId] = useState(null);
  const [slotResponse, setSlotResponse] = useState(null);
  const [selectedDate, setSelectedDate] = useState(null);
  const [currentStep, setCurrentStep] = useState(1);

  const resetFlow = () => {
    setCustomer(null);
    setVehicle(null);
    setServiceCatalogId(null);
    setSlotResponse(null);
    setSelectedDate(null);
    setCurrentStep(1);
  };

  return (
    <AppointmentContext.Provider
      value={{
        centerId,
        setCenterId,
        customer,
        setCustomer,
        vehicle,
        setVehicle,
        serviceCatalogId,
        setServiceCatalogId,
        slotResponse,
        setSlotResponse,
        selectedDate,
        setSelectedDate,
        currentStep,
        setCurrentStep,
        resetFlow,
      }}
    >
      {children}
    </AppointmentContext.Provider>
  );
};
