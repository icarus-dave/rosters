export class Operator {
  id: number;
  firstName: string = '';
  lastName: string = '';
  email: string = '';
  active: boolean = false;
  teams: string[] = [];

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }

  static compare(a,b) {
    if (a.lastName < b.lastName) return -1;
    if (a.lastName > b.lastName) return 1;
    return 0;
  }
}
