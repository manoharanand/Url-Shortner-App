import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-dialog',
  imports: [MatButtonModule],
  templateUrl: './dialog.html',
  styleUrl: './dialog.css',
})
export class DialogComponent {
  constructor(
    public dialogRef: MatDialogRef<DialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { shortUrl: string }
  ) {}

  close(): void {
    this.dialogRef.close();
  }
}
