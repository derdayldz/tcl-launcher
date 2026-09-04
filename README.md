# TCL Launcher — Kurulum Rehberi (PC gerekmez)

Bu proje, hiçbir şey senin bilgisayarında derlenmeden, tamamen GitHub'ın
bulut sunucularında APK'ya dönüştürülecek şekilde hazırlandı.

## 1. Adım — GitHub'a yükle

1. https://github.com adresinden ücretsiz bir hesap aç (yoksa).
2. Sağ üstten **"+" → "New repository"** ile yeni, **Public** bir repo oluştur
   (isim: `tcl-launcher` yazabilirsin). "Add a README" kutusunu **işaretleme**.
3. Oluşan boş repo sayfasında **"uploading an existing file"** linkine tıkla.
4. Sana verdiğim `tcl-launcher.zip` dosyasını önce bilgisayarına indir,
   **zipten çıkar** (unzip), içindeki tüm dosya ve klasörleri (build.gradle.kts,
   app/, .github/ vs.) o yükleme ekranına sürükle-bırak.
5. Alt kısımda **"Commit changes"** butonuna bas.

## 2. Adım — Otomatik derlemeyi izle

1. Repo sayfasında üstteki **"Actions"** sekmesine gir.
2. "Build APK" adında bir işlem otomatik başlamış olacak (yükleme sonrası
   kendiliğinden tetiklenir). Sarı nokta = çalışıyor, yeşil tik = bitti.
3. Bu işlem **3–6 dakika** kadar sürer, sadece bekle.

## 3. Adım — APK'yı indir

1. Yeşil tik olduğunda o işlemin üstüne tıkla.
2. En altta **"Artifacts"** bölümünde **"tcl-launcher-apk"** göreceksin,
   ona tıkla — bir zip iner, içinde `app-debug.apk` var.

## 4. Adım — TV'ye kurma

En kolay yol: TV'de **"Downloader"** adlı uygulamayı Play Store'dan/uygulama
mağazasından kur. Downloader ile APK dosyasını bir bulut linkinden
(Google Drive, Dropbox vs. — APK'yı oraya da atabilirsin) TV'nin kendisine
indirtip kurabilirsin. Alternatif olarak bir USB bellek ile de aktarıp
TV'deki bir dosya yöneticisinden kurabilirsin.

TV önce "bilinmeyen kaynaklardan yükleme" izni isteyebilir, açman gerekir
(Ayarlar → Güvenlik/Uygulamalar bölümünde).

## 5. Adım — Ana ekran yapma

Uygulamayı kurup açtıktan sonra sağ üstteki **⚙ (dişli)** ikonuna gidip
**"Bu uygulamayı Ana Ekran (Home) yap"** seçeneğine bas. Açılan sistem
ekranında bu uygulamayı seç. Artık TV'nin Home tuşuna her bastığında
senin launcher'ın açılacak.

---

## Uygulamayı nasıl kullanırsın (kısa özet)

- Bir satırın **en solundaki** karta gel, **sola iki kez** art arda bas
  → o satıra özel ayar penceresi açılır (gizle/göster, sil, sırasını
  değiştir, uygulama ekle/çıkar).
- Sağ üstteki dişli ikonu → genel ayarlar (arka plan resmi, tema rengi,
  koyu/açık mod, saat gösterimi, yeni satır ekleme).

## Bilinen sınırlamalar (ilk sürüm)

- "İzlenenler" satırı şu an, senin elle seçtiğin uygulamaları gösteriyor —
  Netflix/YouTube gibi uygulamaların içindeki "hangi bölümü izledin" bilgisini
  otomatik çekmiyor (bu, her uygulamanın kendi izin verdiği ölçüde mümkün,
  istersek bir sonraki adımda Android'in Watch Next API'siyle derinleştirebiliriz).
- Uygulama ikonları şu an sadece isim olarak gösteriliyor, gerçek uygulama
  ikonlarını (resim olarak) bir sonraki sürümde ekleyebiliriz.
- Satır başlığını yeniden adlandırma arayüzü basit tutuldu, istersen
  serbest metin girişi ekleyebiliriz.

Bunları ister misin, söyle, bir sonraki sürümde ekleyelim.
