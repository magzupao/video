import dayjs from 'dayjs/esm';

import { IVideo, NewVideo } from './video.model';

export const sampleWithRequiredData: IVideo = {
  id: 22854,
  tieneAudio: true,
  estado: 'COMPLETADO',
  fechaCreacion: dayjs('2025-12-17T22:07'),
};

export const sampleWithPartialData: IVideo = {
  id: 245,
  audioFilename: 'huzzah worthless shrilly',
  tieneAudio: true,
  estado: 'COMPLETADO',
  fechaCreacion: dayjs('2025-12-18T00:27'),
};

export const sampleWithFullData: IVideo = {
  id: 31979,
  titulo: 'discrete attribute marimba',
  audioFilename: 'yahoo',
  tieneAudio: true,
  duracionTransicion: 5059,
  estado: 'ERROR',
  fechaCreacion: dayjs('2025-12-18T09:41'),
  fechaDescarga: dayjs('2025-12-18T18:11'),
};

export const sampleWithNewData: NewVideo = {
  tieneAudio: false,
  estado: 'COMPLETADO',
  fechaCreacion: dayjs('2025-12-18T02:34'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
