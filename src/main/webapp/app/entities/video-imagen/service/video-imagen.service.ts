import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IVideoImagen, NewVideoImagen } from '../video-imagen.model';

export type PartialUpdateVideoImagen = Partial<IVideoImagen> & Pick<IVideoImagen, 'id'>;

export type EntityResponseType = HttpResponse<IVideoImagen>;
export type EntityArrayResponseType = HttpResponse<IVideoImagen[]>;

@Injectable({ providedIn: 'root' })
export class VideoImagenService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/video-imagens');

  create(videoImagen: NewVideoImagen): Observable<EntityResponseType> {
    return this.http.post<IVideoImagen>(this.resourceUrl, videoImagen, { observe: 'response' });
  }

  update(videoImagen: IVideoImagen): Observable<EntityResponseType> {
    return this.http.put<IVideoImagen>(
      `${this.resourceUrl}/${encodeURIComponent(this.getVideoImagenIdentifier(videoImagen))}`,
      videoImagen,
      { observe: 'response' },
    );
  }

  partialUpdate(videoImagen: PartialUpdateVideoImagen): Observable<EntityResponseType> {
    return this.http.patch<IVideoImagen>(
      `${this.resourceUrl}/${encodeURIComponent(this.getVideoImagenIdentifier(videoImagen))}`,
      videoImagen,
      { observe: 'response' },
    );
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IVideoImagen>(`${this.resourceUrl}/${encodeURIComponent(id)}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IVideoImagen[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(id)}`, { observe: 'response' });
  }

  getVideoImagenIdentifier(videoImagen: Pick<IVideoImagen, 'id'>): number {
    return videoImagen.id;
  }

  compareVideoImagen(o1: Pick<IVideoImagen, 'id'> | null, o2: Pick<IVideoImagen, 'id'> | null): boolean {
    return o1 && o2 ? this.getVideoImagenIdentifier(o1) === this.getVideoImagenIdentifier(o2) : o1 === o2;
  }

  addVideoImagenToCollectionIfMissing<Type extends Pick<IVideoImagen, 'id'>>(
    videoImagenCollection: Type[],
    ...videoImagensToCheck: (Type | null | undefined)[]
  ): Type[] {
    const videoImagens: Type[] = videoImagensToCheck.filter(isPresent);
    if (videoImagens.length > 0) {
      const videoImagenCollectionIdentifiers = videoImagenCollection.map(videoImagenItem => this.getVideoImagenIdentifier(videoImagenItem));
      const videoImagensToAdd = videoImagens.filter(videoImagenItem => {
        const videoImagenIdentifier = this.getVideoImagenIdentifier(videoImagenItem);
        if (videoImagenCollectionIdentifiers.includes(videoImagenIdentifier)) {
          return false;
        }
        videoImagenCollectionIdentifiers.push(videoImagenIdentifier);
        return true;
      });
      return [...videoImagensToAdd, ...videoImagenCollection];
    }
    return videoImagenCollection;
  }
}
