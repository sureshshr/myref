export type Department = 'Engineering' | 'HR' | 'Sales' | 'Finance' | 'Operations';

export interface Employee {
  id: string;
  name: string;
  email: string;
  department: Department;
  salary: number;
  active: boolean;
  createdAtIso: string;
  updatedAtIso: string;
}

export const departments: Department[] = [
  'Engineering',
  'HR',
  'Sales',
  'Finance',
  'Operations'
];
