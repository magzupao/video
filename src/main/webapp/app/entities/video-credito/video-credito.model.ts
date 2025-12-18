import { IUser } from 'app/entities/user/user.model';

export interface IVideoCredito {
  id: number;
  videosConsumidos?: number | null;
  videosDisponibles?: number | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewVideoCredito = Omit<IVideoCredito, 'id'> & { id: null };
