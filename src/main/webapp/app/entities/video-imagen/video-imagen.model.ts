import { IVideo } from 'app/entities/video/video.model';

export interface IVideoImagen {
  id: number;
  filename?: string | null;
  orden?: number | null;
  duracionIndividual?: number | null;
  video?: Pick<IVideo, 'id'> | null;
}

export type NewVideoImagen = Omit<IVideoImagen, 'id'> & { id: null };
