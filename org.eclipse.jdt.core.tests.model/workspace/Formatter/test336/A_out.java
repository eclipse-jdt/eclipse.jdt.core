								Image tmp = new Image(display, rect.width,
										rect.height);
								try
								{
									ImageData tmpData = tmp.getImageData();
									for (int y = 0; y < rect.height; y++)
									{
										for (int x = 0; x < rect.width; x++)
										{
											int val;
											val = (x << TestImage.WIDTH_BITS);
											val |= y;
											//											tmpData.setPixels(x, y, val);
											tmpData.setPixels(x, y, 0);
										}
									}
									GC dc = new GC(testImage);
									try
									{
										dc.drawImage(tmp, 0, 0);
									}
									finally
									{
										dc.dispose();
									}
								}
								finally
								{
									tmp.dispose();
								}