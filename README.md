# AndroidPermission
这是一个RxPremission的封装使用类
### 使用方式
```
   PermissionUtil.requestPermission(this, new BasePermissionListener() {

                    @Override
                    public void onGranted() {
                       
                    }

                    @Override
                    public void onCancel() {
                        super.onCancel();
                        finish();
                    }

                    @Override
                    public void onToSetting() {
                        super.onToSetting();
                        finish();
                    }
                },
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
    }
```
