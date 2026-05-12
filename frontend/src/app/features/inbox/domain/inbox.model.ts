export interface InboxItem {
  messageId: string;
  mailbox: string;
  receivedDateTime: string;
  sender: string;
  subject: string;
  summary: string;
  incidenciaGenerada: boolean;
  asignada: boolean;
  tecnicoAsignado: string;
}

export interface Tecnico {
  id: number;
  nombre: string;
  email: string;
}

export interface InboxContext {
  appName: string;
  mailboxNombre: string;
  mailboxCorreo: string;
  usuarioConectado: string;
  permisos: string;
  perfil: 'ADMIN' | 'CONSULTA' | 'RESOLUTOR' | string;
  puedeVerCorreos: boolean;
}

export interface IncidenciaInboxItem {
  id: number;
  messageId: string;
  mailbox: string;
  receivedDateTime: string;
  sender: string;
  subject: string;
  summary: string;
  tecnicoAsignado: string;
  tecnicoEmail: string;
  categoriaId?: number | null;
  categoriaAbreviatura?: string | null;
  categoriaColorHex?: string | null;
  resuelta: boolean;
  enProgreso: boolean;
  assignedAt: string;
}

export interface Categoria {
  id: number;
  nombre: string;
  abreviatura: string;
  colorHex: string;
}

export interface IncidenciasStatsResponse {
  currentMonth: string;
  previousMonth: string;
  categorias: {
    categoriaAbreviatura: string;
    categoriaNombre: string;
    actualTotal: number;
    actualResueltas: number;
    actualSinResolver: number;
    anteriorTotal: number;
    anteriorResueltas: number;
    anteriorSinResolver: number;
  }[];
  tecnicos: {
    tecnicoNombre: string;
    actualAsignadas: number;
    actualResueltas: number;
    anteriorAsignadas: number;
    anteriorResueltas: number;
  }[];
  totalizador: {
    actualTotal: number;
    actualResueltas: number;
    actualSinResolver: number;
    anteriorTotal: number;
    anteriorResueltas: number;
    anteriorSinResolver: number;
  };
}

export interface IncidenciaNota {
  id: number;
  incidenciaId: number;
  tecnico: string;
  observacion: string;
  detalle: string;
  accionRealizada: string;
  createdAt: string;
}
