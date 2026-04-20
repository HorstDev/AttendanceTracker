import { Role } from "./role";

export class User {
    id: string = '';
    email: string = '';
    profiles: Profile[] = []
    roles: Role[] = [];
}

export class Profile {
    id: string = '';
    name: string = ''
}