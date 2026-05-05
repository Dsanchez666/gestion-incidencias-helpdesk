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
}
