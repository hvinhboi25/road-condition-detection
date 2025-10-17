// Simple unsigned upload to Cloudinary for annotated videos
// Uses the same cloud/preset as Android side

const CLOUD_NAME = 'dkcc9skxe';
// Use your newly created unsigned preset name
const UPLOAD_PRESET = 'annotated_unsigned';

export async function uploadAnnotatedVideo(blob, onProgress) {
  const url = `https://api.cloudinary.com/v1_1/${CLOUD_NAME}/video/upload`;
  const form = new FormData();
  form.append('file', blob, 'annotated.webm');
  form.append('upload_preset', UPLOAD_PRESET);
  form.append('folder', 'road_detection/annotated');

  // Use XHR to report progress if provided
  if (onProgress) {
    const xhr = new XMLHttpRequest();
    return await new Promise((resolve, reject) => {
      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          const pct = Math.round((e.loaded / e.total) * 100);
          onProgress(pct);
        }
      };
      xhr.onreadystatechange = () => {
        if (xhr.readyState === 4) {
          if (xhr.status >= 200 && xhr.status < 300) {
            try {
              const res = JSON.parse(xhr.responseText);
              resolve(res.secure_url);
            } catch (e) {
              reject(e);
            }
          } else {
            const msg = xhr.responseText || `status ${xhr.status}`;
            reject(new Error(`Cloudinary upload failed: ${msg}`));
          }
        }
      };
      xhr.open('POST', url, true);
      xhr.send(form);
    });
  }

  const resp = await fetch(url, { method: 'POST', body: form });
  if (!resp.ok) {
    const txt = await resp.text().catch(() => '');
    throw new Error(`Cloudinary upload failed: ${resp.status} ${txt}`);
  }
  const data = await resp.json();
  return data.secure_url;
}


