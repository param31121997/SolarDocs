export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api',
  appName: 'SolarDocs',
  appVersion: '2.0.0',
  enableLogging: true,
  requestTimeout: 30000,
  features: {
    pdfGeneration: true,
    multiLanguage: true,
    backup: true,
    offline: false
  }
};
