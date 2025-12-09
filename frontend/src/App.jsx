import { AppointmentProvider, useAppointment } from './context/AppointmentContext';
import TopBar from './components/TopBar';
import Step1CustomerLookup from './components/steps/Step1CustomerLookup';
import Step2Diagnosis from './components/steps/Step2Diagnosis';
import Step3IntelligenceCheck from './components/steps/Step3IntelligenceCheck';
import Step4CalendarSelection from './components/steps/Step4CalendarSelection';
import Step5Confirmation from './components/steps/Step5Confirmation';

function AppContent() {
  const { currentStep, setCurrentStep } = useAppointment();

  const handleNext = () => {
    if (currentStep < 5) {
      setCurrentStep(currentStep + 1);
    }
  };

  const handleBack = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const handleNewAppointment = () => {
    setCurrentStep(1);
  };

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return <Step1CustomerLookup onNext={handleNext} />;
      case 2:
        return <Step2Diagnosis onNext={handleNext} onBack={handleBack} />;
      case 3:
        return <Step3IntelligenceCheck onNext={handleNext} onBack={handleBack} />;
      case 4:
        return <Step4CalendarSelection onNext={handleNext} onBack={handleBack} />;
      case 5:
        return <Step5Confirmation onNewAppointment={handleNewAppointment} />;
      default:
        return <Step1CustomerLookup onNext={handleNext} />;
    }
  };

  return (
    <div className="min-h-screen bg-slate-100">
      <TopBar />
      <main className="py-8 px-4">
        {/* Step Indicator */}
        {currentStep < 5 && (
          <div className="max-w-4xl mx-auto mb-8">
            <div className="flex items-center justify-between">
              {[1, 2, 3, 4, 5].map((step) => (
                <div key={step} className="flex items-center flex-1">
                  <div className="flex flex-col items-center flex-1">
                    <div
                      className={`w-10 h-10 rounded-full flex items-center justify-center font-semibold ${
                        step === currentStep
                          ? 'bg-blue-600 text-white'
                          : step < currentStep
                          ? 'bg-emerald-600 text-white'
                          : 'bg-gray-300 text-gray-600'
                      }`}
                    >
                      {step < currentStep ? '✓' : step}
                    </div>
                    <div className="mt-2 text-xs font-medium text-gray-600 text-center">
                      {step === 1 && 'Customer'}
                      {step === 2 && 'Diagnosis'}
                      {step === 3 && 'Intelligence'}
                      {step === 4 && 'Calendar'}
                      {step === 5 && 'Confirm'}
                    </div>
                  </div>
                  {step < 5 && (
                    <div
                      className={`h-1 flex-1 mx-2 ${
                        step < currentStep ? 'bg-emerald-600' : 'bg-gray-300'
                      }`}
                    />
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {renderStep()}
      </main>
    </div>
  );
}

function App() {
  return (
    <AppointmentProvider>
      <AppContent />
    </AppointmentProvider>
  );
}

export default App;
