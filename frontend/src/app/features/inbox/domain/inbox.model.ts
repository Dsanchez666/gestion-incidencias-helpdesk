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
  assignedAt: string;
}

export interface Categoria {
  id: number;
  nombre: string;
  abreviatura: string;
}
