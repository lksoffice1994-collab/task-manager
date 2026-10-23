// Add any custom JavaScript here if needed
document.addEventListener('DOMContentLoaded', function () {
  var photoControl = document.querySelector('.profile-photo-control');
  var photoTrigger = document.getElementById('profile-photo-trigger');
  var photoMenu = document.getElementById('profile-photo-menu');
  var updatePhotoButton = document.getElementById('update-profile-photo');
  var photoDialog = document.getElementById('profile-photo-dialog');
  var closeDialogButton = document.getElementById('close-photo-dialog');
  var choosePhotoButton = document.getElementById('choose-profile-photo');
  var photoInput = document.getElementById('profile-photo-file');
  var photoPreview = document.getElementById('photo-preview');
  var profilePhoto = document.getElementById('profile-photo');
  var savePhotoButton = document.getElementById('save-profile-photo');
  var uploadMessage = document.getElementById('photo-upload-message');

  if (!photoControl || !photoTrigger || !photoDialog || !profilePhoto) {
    return;
  }

  var userIdElement = document.querySelector('.user-greeting span');
  var userId = userIdElement ? userIdElement.textContent.trim() : '';
  var photoEndpoint = document.body.getAttribute('data-profile-photo-endpoint');
  var maxPhotoSize = 1024 * 1024;
  var pendingPhoto = '';
  var pendingFile = null;

  function getAuthorization() {
    var jwtInput = document.querySelector('input[name="jwt"]');
    return jwtInput && jwtInput.value ? 'Bearer ' + jwtInput.value : '';
  }

  function getPhotoUrl() {
    return photoEndpoint + '/' + encodeURIComponent(userId);
  }

  function loadSavedPhoto() {
    var authorization = getAuthorization();
    if (!authorization || !userId || !photoEndpoint) {
      return;
    }
    fetch(getPhotoUrl(), { headers: { Authorization: authorization } })
      .then(function (response) {
        if (!response.ok || response.status === 204) {
          return null;
        }
        var contentType = response.headers.get('Content-Type') || '';
        return contentType.indexOf('image/') === 0 ? response.blob() : null;
      })
      .then(function (imageBlob) {
        if (imageBlob && imageBlob.size > 0) {
          profilePhoto.src = URL.createObjectURL(imageBlob);
          profilePhoto.alt = 'Profile photo';
        }
      })
      .catch(function () {
        // Keep the default avatar visible if photo retrieval fails.
      });
  }

  loadSavedPhoto();

  function setMenuOpen(open) {
    photoMenu.hidden = !open;
    photoTrigger.setAttribute('aria-expanded', String(open));
  }

  function clearMessage() {
    uploadMessage.textContent = '';
    uploadMessage.classList.remove('error');
  }

  photoTrigger.addEventListener('click', function () {
    setMenuOpen(photoMenu.hidden);
  });

  document.addEventListener('click', function (event) {
    if (!photoControl.contains(event.target)) {
      setMenuOpen(false);
    }
  });

  updatePhotoButton.addEventListener('click', function () {
    setMenuOpen(false);
    photoPreview.src = profilePhoto.src;
    photoInput.value = '';
    pendingPhoto = '';
    pendingFile = null;
    savePhotoButton.disabled = true;
    clearMessage();
    photoDialog.showModal();
  });

  closeDialogButton.addEventListener('click', function () {
    photoDialog.close();
  });

  choosePhotoButton.addEventListener('click', function () {
    photoInput.click();
  });

  photoInput.addEventListener('change', function () {
    clearMessage();
    savePhotoButton.disabled = true;
    pendingPhoto = '';
    pendingFile = null;
    var file = photoInput.files && photoInput.files[0];
    if (!file) {
      return;
    }

    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      uploadMessage.textContent = 'Choose a JPG, PNG, or WebP image.';
      uploadMessage.classList.add('error');
      photoInput.value = '';
      return;
    }

    if (file.size > maxPhotoSize) {
      uploadMessage.textContent = 'The photo must be 1 MB or smaller.';
      uploadMessage.classList.add('error');
      photoInput.value = '';
      return;
    }

    var reader = new FileReader();
    reader.addEventListener('load', function () {
      if (typeof reader.result === 'string') {
        pendingPhoto = reader.result;
        pendingFile = file;
        photoPreview.src = pendingPhoto;
        savePhotoButton.disabled = false;
        uploadMessage.textContent = file.name;
      }
    });
    reader.addEventListener('error', function () {
      uploadMessage.textContent = 'Could not read that image. Please choose another file.';
      uploadMessage.classList.add('error');
    });
    reader.readAsDataURL(file);
  });

  savePhotoButton.addEventListener('click', function () {
    var authorization = getAuthorization();
    if (!pendingPhoto || !pendingFile || !authorization || !userId) {
      uploadMessage.textContent = 'Please sign in again before updating your photo.';
      uploadMessage.classList.add('error');
      return;
    }
    savePhotoButton.disabled = true;
    uploadMessage.textContent = 'Uploading photo…';
    var formData = new FormData();
    formData.append('photo', pendingFile);
    fetch(getPhotoUrl(), {
      method: 'PUT',
      headers: { Authorization: authorization },
      body: formData
    }).then(function (response) {
      if (!response.ok) {
        throw new Error('Photo upload failed');
      }
      profilePhoto.src = pendingPhoto;
      profilePhoto.alt = 'Profile photo';
      photoDialog.close();
      pendingFile = null;
    }).catch(function () {
      savePhotoButton.disabled = false;
      uploadMessage.textContent = 'Could not upload the photo. Please try again.';
      uploadMessage.classList.add('error');
    });
  });

  photoDialog.addEventListener('click', function (event) {
    if (event.target === photoDialog) {
      photoDialog.close();
    }
  });
});
