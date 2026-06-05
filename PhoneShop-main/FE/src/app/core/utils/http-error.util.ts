import { HttpErrorResponse } from '@angular/common/http';

export function extractApiErrorMessage(error: unknown, fallbackMessage: string): string {
  if (error instanceof HttpErrorResponse) {
    const payload = error.error as
      | { message?: string; error?: string; detail?: string }
      | string
      | null
      | undefined;

    if (typeof payload === 'string' && payload.trim()) {
      return payload;
    }

    if (payload && typeof payload === 'object') {
      if (typeof payload.message === 'string' && payload.message.trim()) {
        return payload.message;
      }

      if (typeof payload.detail === 'string' && payload.detail.trim()) {
        return payload.detail;
      }

      if (typeof payload.error === 'string' && payload.error.trim()) {
        return payload.error;
      }
    }

    if (error.message?.trim()) {
      return error.message;
    }
  }

  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }

  return fallbackMessage;
}
