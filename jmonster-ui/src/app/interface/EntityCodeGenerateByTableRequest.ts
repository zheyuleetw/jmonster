import { Column } from "./Column";

export interface EntityCodeGenerateByTableRequest {
    tableName: string;
    enversAudit: boolean;
    columns: Column[];
  }