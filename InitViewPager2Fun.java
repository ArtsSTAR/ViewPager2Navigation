        mViewPage2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                widget.setAnimCircleBezierPathCenterPointCoordinate(positionOffset,position);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if(state == 0){
                    widget.setAnimCircleBezierPathCenterPointCoordinate(0F,mPosition);
                }
            }
        });
