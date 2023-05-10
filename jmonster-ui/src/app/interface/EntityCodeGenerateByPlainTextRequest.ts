import { Column } from "./Column";

export interface EntityCodeGenerateByPlainTextRequest {
    tableName: string;
    enversAudit: boolean;
    text: string;
  }