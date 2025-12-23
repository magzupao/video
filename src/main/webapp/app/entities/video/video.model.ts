import dayjs from 'dayjs/esm';

import { EstadoVideo } from 'app/entities/enumerations/estado-video.model';
import { IUser } from 'app/entities/user/user.model';

export interface IVideo {
  id: number;
  titulo?: string | null;
  audioFilename?: string | null;
  tieneAudio?: boolean | null;
  duracionTransicion?: number | null;
  estado?: keyof typeof EstadoVideo | null;
  fechaCreacion?: dayjs.Dayjs | null;
  fechaDescarga?: dayjs.Dayjs | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
  downloadUrl?: string | null;
  outputFilename?: string | null;
}

export type NewVideo = Omit<IVideo, 'id'> & { id: null };
