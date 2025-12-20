export interface Student {
  id: string;
  name: string;
  email: string;
  major: string;
  year: number;
  active: boolean;
  createdAtIso: string;
  updatedAtIso: string;
}

export type StudentCreate = Omit<Student, 'id' | 'createdAtIso' | 'updatedAtIso'>;
export type StudentUpdate = Omit<Student, 'createdAtIso' | 'updatedAtIso'>;
