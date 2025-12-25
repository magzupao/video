import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IVideoCredito, NewVideoCredito } from '../video-credito.model';

export type PartialUpdateVideoCredito = Partial<IVideoCredito> & Pick<IVideoCredito, 'id'>;

export type EntityResponseType = HttpResponse<IVideoCredito>;
export type EntityArrayResponseType = HttpResponse<IVideoCredito[]>;

@Injectable({ providedIn: 'root' })
export class VideoCreditoService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/video-creditos');

  create(videoCredito: NewVideoCredito): Observable<EntityResponseType> {
    return this.http.post<IVideoCredito>(this.resourceUrl, videoCredito, { observe: 'response' });
  }

  update(videoCredito: IVideoCredito): Observable<EntityResponseType> {
    return this.http.put<IVideoCredito>(
      `${this.resourceUrl}/${encodeURIComponent(this.getVideoCreditoIdentifier(videoCredito))}`,
      videoCredito,
      { observe: 'response' },
    );
  }

  partialUpdate(videoCredito: PartialUpdateVideoCredito): Observable<EntityResponseType> {
    return this.http.patch<IVideoCredito>(
      `${this.resourceUrl}/${encodeURIComponent(this.getVideoCreditoIdentifier(videoCredito))}`,
      videoCredito,
      { observe: 'response' },
    );
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IVideoCredito>(`${this.resourceUrl}/${encodeURIComponent(id)}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IVideoCredito[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(id)}`, { observe: 'response' });
  }

  getVideoCreditoIdentifier(videoCredito: Pick<IVideoCredito, 'id'>): number {
    return videoCredito.id;
  }

  /**
   * Get the videoCredito of the current authenticated user.
   * @returns Observable with the current user's video credits
   */
  getCurrentUserCredits(): Observable<EntityResponseType> {
    return this.http.get<IVideoCredito>(`${this.resourceUrl}/current-user`, { observe: 'response' });
  }

  compareVideoCredito(o1: Pick<IVideoCredito, 'id'> | null, o2: Pick<IVideoCredito, 'id'> | null): boolean {
    return o1 && o2 ? this.getVideoCreditoIdentifier(o1) === this.getVideoCreditoIdentifier(o2) : o1 === o2;
  }

  addVideoCreditoToCollectionIfMissing<Type extends Pick<IVideoCredito, 'id'>>(
    videoCreditoCollection: Type[],
    ...videoCreditosToCheck: (Type | null | undefined)[]
  ): Type[] {
    const videoCreditos: Type[] = videoCreditosToCheck.filter(isPresent);
    if (videoCreditos.length > 0) {
      const videoCreditoCollectionIdentifiers = videoCreditoCollection.map(videoCreditoItem =>
        this.getVideoCreditoIdentifier(videoCreditoItem),
      );
      const videoCreditosToAdd = videoCreditos.filter(videoCreditoItem => {
        const videoCreditoIdentifier = this.getVideoCreditoIdentifier(videoCreditoItem);
        if (videoCreditoCollectionIdentifiers.includes(videoCreditoIdentifier)) {
          return false;
        }
        videoCreditoCollectionIdentifiers.push(videoCreditoIdentifier);
        return true;
      });
      return [...videoCreditosToAdd, ...videoCreditoCollection];
    }
    return videoCreditoCollection;
  }
}
