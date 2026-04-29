/**
 * Folder returned by Graph tracing.
 */
export interface MailFolder {
  id?: string | null;
  displayName?: string | null;
}

/**
 * Mailbox result returned by Graph tracing.
 */
export interface MailboxFolderResult {
  id?: string | null;
  nombre?: string | null;
  direccionCorreo?: string | null;
  status?: string | null;
  error?: string | null;
  folders?: MailFolder[] | null;
}

/**
 * Graph trace response with folders and execution traces.
 */
export interface GraphTraceResponse {
  success: boolean;
  traces?: string[] | null;
  mailboxes?: MailboxFolderResult[] | null;
  error?: string | null;
}
