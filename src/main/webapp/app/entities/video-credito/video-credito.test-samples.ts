import { IVideoCredito, NewVideoCredito } from './video-credito.model';

export const sampleWithRequiredData: IVideoCredito = {
  id: 5035,
  videosConsumidos: 15906,
  videosDisponibles: 15835,
};

export const sampleWithPartialData: IVideoCredito = {
  id: 28866,
  videosConsumidos: 9021,
  videosDisponibles: 27250,
};

export const sampleWithFullData: IVideoCredito = {
  id: 14277,
  videosConsumidos: 5451,
  videosDisponibles: 27741,
};

export const sampleWithNewData: NewVideoCredito = {
  videosConsumidos: 15928,
  videosDisponibles: 15157,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
