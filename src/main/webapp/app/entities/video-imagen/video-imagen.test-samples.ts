import { IVideoImagen, NewVideoImagen } from './video-imagen.model';

export const sampleWithRequiredData: IVideoImagen = {
  id: 12417,
  filename: 'doubtfully',
  orden: 3,
};

export const sampleWithPartialData: IVideoImagen = {
  id: 25542,
  filename: 'pfft usually and',
  orden: 5,
};

export const sampleWithFullData: IVideoImagen = {
  id: 30562,
  filename: 'what hollow',
  orden: 9,
  duracionIndividual: 27234,
};

export const sampleWithNewData: NewVideoImagen = {
  filename: 'modulo mmm',
  orden: 9,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
