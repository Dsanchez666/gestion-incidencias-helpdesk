export interface Incidencia {
  id?: string;
  asunto: string;
  descripcion: string;
  emailSolicitante: string;
  prioridad: 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA';
  estado?: 'ABIERTA' | 'EN_PROGRESO' | 'CERRADA';
  creadaEn?: string;
}
