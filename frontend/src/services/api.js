import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const searchCustomers = async (query) => {
  if (!query) return [];

  const response = await api.get('/customers/search', {
    params: { q: query },
  });
  return response.data;
};

export const findSlot = async (serviceCatalogId, centerId) => {
  const response = await api.post('/schedule/find-slot', {
    serviceCatalogId,
    centerId,
  });
  return response.data;
};

export const bookAppointment = async ({
  customerId,
  vehicleId,
  serviceCatalogId,
  centerId,
  startTime,
  emergency = false,
}) => {
  const response = await api.post('/schedule/book', {
    customerId,
    vehicleId,
    serviceCatalogId,
    centerId,
    startTime,
    emergency,
  });
  return response.data;
};

export default api;
