# Tests - Gestiona-T

Tests transversales del proyecto.

## Tipos de tests

- **integration/**: Tests de integracion entre servicios
- **e2e/**: Tests end-to-end (flujos completos)
- **performance/**: Tests de carga y estres

## Ejecutar tests

### Frontend

    cd frontend
    npm test

### Backend-Core

    cd backend-core
    mvn test

### Backend-AI

    cd backend-ai
    pytest

## Cobertura

Se requiere minimo 80% de cobertura en todos los servicios.