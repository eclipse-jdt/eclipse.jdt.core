
{
    if (size < currentSize)
    {
        try
        {
            size = inStream.available();
        }
        catch (IOException e)
        {
        }
    }
    else if (size == currentSize)
    {
        ++size;
    }
    else
    {
        --size;
    }
}