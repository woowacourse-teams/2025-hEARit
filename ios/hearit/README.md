# hearit

hEARit (IT를 듣다) - A podcast platform for developers and IT professionals.

## Environment Setup

Before running the project, you need to set up your environment variables:

1. Copy the `.env.example` file to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Fill in your Kakao Native App Key in the `.env` file:
   ```env
   KAKAO_NATIVE_APP_KEY=your_kakao_native_app_key_here
   ```
   
   You can get your Kakao Native App Key from [Kakao Developers Console](https://developers.kakao.com/console/app).

3. Run `flutter pub get` to install dependencies.

⚠️ **Important**: Never commit the `.env` file to version control. It's already added to `.gitignore`.

## Getting Started

This project is a starting point for a Flutter application.

A few resources to get you started if this is your first Flutter project:

- [Lab: Write your first Flutter app](https://docs.flutter.dev/get-started/codelab)
- [Cookbook: Useful Flutter samples](https://docs.flutter.dev/cookbook)

For help getting started with Flutter development, view the
[online documentation](https://docs.flutter.dev/), which offers tutorials,
samples, guidance on mobile development, and a full API reference.
